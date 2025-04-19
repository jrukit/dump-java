package helper;

import org.mockito.ArgumentMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;

public class DeepEqMatcher<T> implements ArgumentMatcher<T> {
    private final T expected;
    private final String[] excludedFields;

    public DeepEqMatcher(T expected, String... excludedFields) {
        this.expected = expected;
        this.excludedFields = excludedFields;
    }

    @Override
    public boolean matches(Object argument) {
        try {
            assertThat(argument)
                    .usingRecursiveComparison()
                    .ignoringFields(excludedFields)
                    .isEqualTo(expected);
            return true;
        } catch (Throwable e) {
            System.err.println("Argument mismatch. Please check:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Object argument that is deep-equal to the given value with support for excluding
     * selected fields from a class.
     *
     * <p>
     * This matcher can be used when equals() is not implemented on compared objects.
     * Matcher uses assertj usingRecursiveComparison of assertThat API to compare fields recursively of wanted and actual object.
     * </p>
     *
     * @param expected       the given expected value.
     * @param excludedFields fields to exclude, if field does not exist it is ignored.
     * @return <code>argThat(new DeepReflectionEqMatcher<>(expected, excludedFields));</code>
     */
    public static <T> T deepEq(T expected, String... excludedFields) {
        return argThat(new DeepEqMatcher<>(expected, excludedFields));
    }
}
