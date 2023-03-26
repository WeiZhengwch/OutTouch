package net.minecraft.client.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class JsonException extends IOException {
    private final List<JsonException.Entry> field_151383_a = Lists.newArrayList();
    private final String exceptionMessage;

    public JsonException(String message) {
        field_151383_a.add(new JsonException.Entry());
        exceptionMessage = message;
    }

    public JsonException(String message, Throwable cause) {
        super(cause);
        field_151383_a.add(new JsonException.Entry());
        exceptionMessage = message;
    }

    public static JsonException func_151379_a(Exception p_151379_0_) {
        if (p_151379_0_ instanceof JsonException) {
            return (JsonException) p_151379_0_;
        } else {
            String s = p_151379_0_.getMessage();

            if (p_151379_0_ instanceof FileNotFoundException) {
                s = "File not found";
            }

            return new JsonException(s, p_151379_0_);
        }
    }

    public void func_151380_a(String p_151380_1_) {
        field_151383_a.get(0).func_151373_a(p_151380_1_);
    }

    public void func_151381_b(String p_151381_1_) {
        field_151383_a.get(0).field_151376_a = p_151381_1_;
        field_151383_a.add(0, new JsonException.Entry());
    }

    public String getMessage() {
        return "Invalid " + field_151383_a.get(field_151383_a.size() - 1).toString() + ": " + exceptionMessage;
    }

    public static class Entry {
        private final List<String> field_151375_b;
        private String field_151376_a;

        private Entry() {
            field_151376_a = null;
            field_151375_b = Lists.newArrayList();
        }

        private void func_151373_a(String p_151373_1_) {
            field_151375_b.add(0, p_151373_1_);
        }

        public String func_151372_b() {
            return StringUtils.join(field_151375_b, "->");
        }

        public String toString() {
            return field_151376_a != null ? (!field_151375_b.isEmpty() ? field_151376_a + " " + func_151372_b() : field_151376_a) : (!field_151375_b.isEmpty() ? "(Unknown file) " + func_151372_b() : "(Unknown file)");
        }
    }
}
