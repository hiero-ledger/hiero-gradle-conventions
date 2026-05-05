// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import java.io.Serializable;

public class ProtoLintStep {
    private static final String NAME = "ProtoCommentLinter";

    public static FormatterStep create() {
        return FormatterStep.create(NAME, State.INSTANCE, State::toFormatter);
    }

    private static class State implements Serializable {
        static final State INSTANCE = new State();

        FormatterFunc toFormatter() {
            return ProtoCommentLinter::lint;
        }
    }
}
