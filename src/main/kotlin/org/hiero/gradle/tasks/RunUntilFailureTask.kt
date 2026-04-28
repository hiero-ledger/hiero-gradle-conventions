package org.hiero.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.tooling.GradleConnector
import java.io.File

/**
 * A custom Gradle task that runs a specified test task repeatedly until a failure occurs or 
 * a maximum number of retries is reached.
 *
 * This task provides the ability to configure test task names, apply test filters, and limit 
 * the number of retries. It can be used to identify flaky tests or repeatedly validate test execution.
 *
 * The task launches the target task in a fresh sub-build via the Gradle Tooling API to ensure 
 * UP-TO-DATE caching never skips execution.
 *
 * Properties:
 * - `testTaskName`: The name of the test task to run until failure.
 * - `maxRetries`: The maximum number of retries allowed. A value of `0` indicates unlimited retries.
 * - `testFilters`: A list of test filter patterns forwarded to the test task. These patterns follow 
 *   the syntax supported by Gradle's built-in test filtering (e.g., class name, method name, 
 *   wildcard patterns). Optional.
 *
 * Task Behavior:
 * - The task will execute the specified test task in a loop until either a test failure occurs 
 *   or the configured maximum retry count is reached.
 * - Each execution uses `--rerun-tasks` to bypass UP-TO-DATE checks and ensure fresh execution.
 * - If a failure is detected during an execution, the task stops and throws a `GradleException`.
 * - Test filter patterns can be specified for precisely targeting specific tests during execution.
 *
 * Usage Notes:
 * - Configure the properties either via the `runUntilFailure` extension or as command-line
 *   options when invoking the task.
 * - Ensure that the target test task exists and is correctly named in the configuration.
 * - To test local changes to this plugin in another project, use `--include-build` to
 *   override the published conventions with your local checkout:
 *   ```
 *   ./gradlew :runUntilFailure \
 *       --include-build /path/to/hiero-gradle-conventions \
 *       --testTaskName :my-module:test \
 *       --tests "com.example.FlakyTest" \
 *       --maxRetries 5
 *   ```
 */
abstract class RunUntilFailureTask : DefaultTask() {

    @get:Input
    @get:Option(
        option = "testTaskName",
        description = "The name of the test task to run until failure."
    )
    abstract val testTaskName: Property<String>

    @get:Input
    @get:Option(option = "maxRetries", description = "Maximum number of retries (0 = unlimited).")
    abstract val maxRetries: Property<Int>

    /**
     * One or more test filter patterns forwarded to the target task via `--tests`.
     * Accepts the same syntax as Gradle's built-in test filtering:
     *
     *   --tests "com.example.MyTest"               (whole class)
     *   --tests "com.example.MyTest.myMethod"       (single method)
     *   --tests "com.example.*"                     (package wildcard)
     *   --tests "MyTest"                            (simple class name)
     *
     * May be specified multiple times on the command line:
     *   ./gradlew runUntilFailure \
     *       --tests "com.example.FlakyTest.methodA" \
     *       --tests "com.example.FlakyTest.methodB"
     *
     * Or set via the extension:
     *   runUntilFailure {
     *       testFilters.set(listOf("com.example.FlakyTest.methodA"))
     *   }
     */
    @get:Input
    @get:Optional
    @get:Option(
        option = "tests",
        description = "Test filter pattern(s) forwarded to the target task (repeatable)."
    )
    abstract val testFilters: ListProperty<String>

    @get:Input
    abstract val projectDir: Property<File>

    /**
     * The Gradle path of the project hosting this task.
     * Used to resolve relative [testTaskName] values to absolute task paths in
     * the Tooling API sub-build.  Empty string means root project.
     */
    @get:Input
    abstract val hostProjectPath: Property<String>

    /**
     * Executes the configured test task repeatedly until a failure occurs or the maximum retry count is reached.
     */
    @TaskAction
    fun runUntilFailure() {
        val taskName = resolveTaskName(testTaskName.get())
        val max = maxRetries.get()
        val unlimited = max == 0
        val filters = testFilters.get()

        printHorizontalRule()
        logger.lifecycle("▶ [RunUntilFailure] targeting task '$taskName'")
        logger.lifecycle("  Max retries  : ${if (unlimited) "unlimited" else max}")
        if (filters.isNotEmpty()) {
            logger.lifecycle("  Test filters : ${filters.joinToString(", ")}")
        }
        printHorizontalRule()

        var attempt = 0

        while (true) {
            attempt++
            val label = if (unlimited) "attempt $attempt" else "attempt $attempt / $max"
            logger.lifecycle("")
            logger.lifecycle("──────────────────────────────────────────────────────────")
            logger.lifecycle("🔁  Run $label")
            logger.lifecycle("──────────────────────────────────────────────────────────")

            val result = executeTask(taskName, filters)

            if (result.isFailure) {
                logger.lifecycle("")
                printHorizontalRule()
                logger.lifecycle("❌  FAILURE detected on $label — stopping.")
                printHorizontalRule()
                throw GradleException(
                    "Task '$taskName' failed on $label. " +
                            "See test reports for details."
                )
            }

            logger.lifecycle("✅  $label passed.")

            if (!unlimited && attempt >= max) {
                logger.lifecycle("")
                printHorizontalRule()
                logger.lifecycle("🏁  Reached max retries ($max) without a failure.")
                printHorizontalRule()
                break
            }
        }
    }

    /**
     * Launches the target task in a fresh sub-build via the Gradle Tooling API
     * so that UP-TO-DATE caching never skips execution.
     *
     * [filters] are forwarded as repeated `--tests <pattern>` arguments, which
     * Gradle's Test task natively understands.
     */
    private fun executeTask(taskName: String, filters: List<String>): Result<Unit> {
        return runCatching {
            val connection = GradleConnector
                .newConnector()
                .forProjectDirectory(projectDir.get())
                .connect()

            connection.use { conn ->
                // --rerun-tasks disables UP-TO-DATE checks so the test task
                // actually executes on every iteration.
                // Task-specific options like --tests must be passed alongside task
                // names via withArguments so Gradle's CLI parser can associate
                // the option with the preceding test task.
                val args = mutableListOf(taskName, "--rerun-tasks")
                filters.forEach {
                    args.add("--tests")
                    args.add(it)
                }

                conn.newBuild()
                    .withArguments(args)
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
            }
        }
    }

    /**
     * If [taskName] is already absolute (starts with `:`), return it as-is.
     * Otherwise prepend [hostProjectPath] so the Tooling API sub-build
     * resolves it against the correct subproject.
     */
    private fun resolveTaskName(taskName: String): String {
        if (taskName.startsWith(':')) return taskName
        val host = hostProjectPath.get()
        return if (host.isEmpty() || host == ":") taskName else "$host:$taskName"
    }

    private fun printHorizontalRule() {
        logger.lifecycle("=".repeat(60))
    }
}
