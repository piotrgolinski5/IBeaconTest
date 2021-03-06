package pl.gg.ibeaconlibrary.utils;

import java.util.Collection;

public final class SDKPreconditions
{
    public static void checkArgument(boolean expression)
    {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expression, Object errorMessage)
    {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static <E extends Exception> void checkArgument(boolean expression, E exception)
            throws Exception
    {
        if (!expression) {
            throw exception;
        }
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs)
    {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static void checkState(boolean expression)
    {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    public static <E extends Exception> void checkState(boolean expression, E exception)
            throws Exception
    {
        if (!expression) {
            throw exception;
        }
    }

    public static void checkState(boolean expression, Object errorMessage)
    {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs)
    {
        if (!expression) {
            throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static <T> T checkNotNull(T reference)
    {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, Object errorMessage)
    {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs)
    {
        if (reference == null) {
            throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }

    public static <T, K extends RuntimeException> T[] checkNotAllNull(K exception, T... references)
    {
        if (exception == null) {
            throw new NullPointerException("Exception cannot be null");
        }
        if (references == null) {
            throw new NullPointerException("References cannot be null");
        }
        int length = references.length;
        int foundNullReferences = 0;
        for (T reference : references) {
            if (reference == null) {
                foundNullReferences++;
            }
        }
        if (foundNullReferences == length) {
            throw exception;
        }
        return references;
    }

    public static <T> void checkNotNullOrEmpty(Collection<T> collection, String message)
    {
        if ((collection == null) || (collection.isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkNotNullOrEmpty(String target, String message)
    {
        if ((target == null) || (target.isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

    public static int checkElementIndex(int index, int size)
    {
        return checkElementIndex(index, size, "index");
    }

    public static int checkElementIndex(int index, int size, String desc)
    {
        if ((index < 0) || (index >= size)) {
            throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
        }
        return index;
    }

    private static String badElementIndex(int index, int size, String desc)
    {
        if (index < 0) {
            return format("%s (%s) must not be negative", new Object[] { desc, Integer.valueOf(index) });
        }
        if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        }
        return format("%s (%s) must be less than size (%s)", new Object[] { desc, Integer.valueOf(index), Integer.valueOf(size) });
    }

    public static int checkPositionIndex(int index, int size)
    {
        return checkPositionIndex(index, size, "index");
    }

    public static int checkPositionIndex(int index, int size, String desc)
    {
        if ((index < 0) || (index > size)) {
            throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
        }
        return index;
    }

    private static String badPositionIndex(int index, int size, String desc)
    {
        if (index < 0) {
            return format("%s (%s) must not be negative", new Object[] { desc, Integer.valueOf(index) });
        }
        if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        }
        return format("%s (%s) must not be greater than size (%s)", new Object[] { desc, Integer.valueOf(index), Integer.valueOf(size) });
    }

    public static void checkPositionIndexes(int start, int end, int size)
    {
        if ((start < 0) || (end < start) || (end > size)) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size)
    {
        if ((start < 0) || (start > size)) {
            return badPositionIndex(start, size, "start index");
        }
        if ((end < 0) || (end > size)) {
            return badPositionIndex(end, size, "end index");
        }
        return format("end index (%s) must not be less than start index (%s)", new Object[] { Integer.valueOf(end), Integer.valueOf(start) });
    }

    private static String format(String template, Object... args)
    {
        template = String.valueOf(template);

        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length)
        {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[(i++)]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));
        if (i < args.length)
        {
            builder.append(" [");
            builder.append(args[(i++)]);
            while (i < args.length)
            {
                builder.append(", ");
                builder.append(args[(i++)]);
            }
            builder.append(']');
        }
        return builder.toString();
    }
}
