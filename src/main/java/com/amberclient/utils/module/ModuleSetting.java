package com.amberclient.utils.module;

import java.util.Set;

public class ModuleSetting {
    public enum SettingType {
        BOOLEAN,
        INTEGER,
        DOUBLE,
        STRING,
        ENUM,
        SET
    }

    private final String name;
    private final String description;
    private final SettingType type;
    private Object value;
    private Object defaultValue;
    private Number minValue;
    private Number maxValue;
    private Number stepValue;

    // Existing constructors
    public ModuleSetting(String name, String description, boolean defaultValue) {
        this.name = name;
        this.description = description;
        this.type = SettingType.BOOLEAN;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ModuleSetting(String name, String description, int defaultValue, int minValue, int maxValue) {
        this.name = name;
        this.description = description;
        this.type = SettingType.INTEGER;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepValue = 1;
    }

    public ModuleSetting(String name, String description, int defaultValue) {
        this(name, description, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public ModuleSetting(String name, String description, double defaultValue, double minValue, double maxValue, double step) {
        this.name = name;
        this.description = description;
        this.type = SettingType.DOUBLE;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepValue = step;
    }

    public ModuleSetting(String name, String description, double defaultValue, double minValue, double maxValue) {
        this(name, description, defaultValue, minValue, maxValue, 0.1);
    }

    public ModuleSetting(String name, String description, double defaultValue) {
        this(name, description, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, 0.1);
    }

    public ModuleSetting(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.type = SettingType.STRING;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    // New constructor for enums
    public ModuleSetting(String name, String description, Enum<?> defaultValue) {
        this.name = name;
        this.description = description;
        this.type = SettingType.ENUM;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    // New constructor for sets
    public ModuleSetting(String name, String description, Set<?> defaultValue) {
        this.name = name;
        this.description = description;
        this.type = SettingType.SET;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    // Existing getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SettingType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Number getStepValue() {
        return stepValue;
    }

    public boolean getBooleanValue() {
        if (type == SettingType.BOOLEAN) {
            return (boolean) value;
        }
        throw new IllegalStateException("Setting is not a boolean");
    }

    public int getIntegerValue() {
        if (type == SettingType.INTEGER) {
            return (int) value;
        }
        throw new IllegalStateException("Setting is not an integer");
    }

    public double getDoubleValue() {
        if (type == SettingType.DOUBLE) {
            return (double) value;
        }
        throw new IllegalStateException("Setting is not a double");
    }

    public String getStringValue() {
        if (type == SettingType.STRING) {
            return (String) value;
        }
        throw new IllegalStateException("Setting is not a string");
    }

    // New getter for enums
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnumValue() {
        if (type == SettingType.ENUM) {
            return (T) value;
        }
        throw new IllegalStateException("Setting is not an enum");
    }

    // New getter for sets
    @SuppressWarnings("unchecked")
    public <T> Set<T> getSetValue() {
        if (type == SettingType.SET) {
            return (Set<T>) value;
        }
        throw new IllegalStateException("Setting is not a set");
    }

    public boolean isEnabled() {
        if (type == SettingType.BOOLEAN) {
            return (boolean) value;
        }
        throw new IllegalStateException("Setting is not a boolean and cannot be checked as enabled/disabled");
    }

    // Existing setters
    public void setBooleanValue(boolean value) {
        if (type == SettingType.BOOLEAN) {
            this.value = value;
        } else {
            throw new IllegalStateException("Setting is not a boolean");
        }
    }

    public void setIntegerValue(int value) {
        if (type == SettingType.INTEGER) {
            if (minValue != null && value < minValue.intValue()) {
                value = minValue.intValue();
            }
            if (maxValue != null && value > maxValue.intValue()) {
                value = maxValue.intValue();
            }

            if (stepValue != null && stepValue.intValue() > 0) {
                int step = stepValue.intValue();
                int min = minValue != null ? minValue.intValue() : 0;

                int stepsFromMin = (value - min) / step;
                int roundedSteps = Math.round((float)(value - min) / step);

                value = min + (roundedSteps * step);

                if (minValue != null && value < minValue.intValue()) {
                    value = minValue.intValue();
                }
                if (maxValue != null && value > maxValue.intValue()) {
                    value = maxValue.intValue();
                }
            }

            this.value = value;
        } else {
            throw new IllegalStateException("Setting is not an integer");
        }
    }

    public void setDoubleValue(double value) {
        if (type == SettingType.DOUBLE) {
            if (minValue != null && value < minValue.doubleValue()) {
                value = minValue.doubleValue();
            }
            if (maxValue != null && value > maxValue.doubleValue()) {
                value = maxValue.doubleValue();
            }

            if (stepValue != null && stepValue.doubleValue() > 0) {
                double step = stepValue.doubleValue();
                double min = minValue != null ? minValue.doubleValue() : 0.0;

                double stepsFromMin = (value - min) / step;
                double roundedSteps = Math.round(stepsFromMin);

                value = min + (roundedSteps * step);

                if (minValue != null && value < minValue.doubleValue()) {
                    value = minValue.doubleValue();
                }
                if (maxValue != null && value > maxValue.doubleValue()) {
                    value = maxValue.doubleValue();
                }
            }

            this.value = value;
        } else {
            throw new IllegalStateException("Setting is not a double");
        }
    }

    public void setStringValue(String value) {
        if (type == SettingType.STRING) {
            this.value = value != null ? value : "";
        } else {
            throw new IllegalStateException("Setting is not a string");
        }
    }

    // New setter for enums
    public void setEnumValue(Enum<?> value) {
        if (type == SettingType.ENUM) {
            this.value = value;
        } else {
            throw new IllegalStateException("Setting is not an enum");
        }
    }

    // New setter for sets
    public <T> void setSetValue(Set<T> value) {
        if (type == SettingType.SET) {
            this.value = value;
        } else {
            throw new IllegalStateException("Setting is not a set");
        }
    }

    public void setValue(Object value) {
        switch (type) {
            case BOOLEAN:
                if (value instanceof Boolean) {
                    setBooleanValue((Boolean) value);
                }
                break;
            case INTEGER:
                if (value instanceof Integer) {
                    setIntegerValue((Integer) value);
                } else if (value instanceof Number) {
                    setIntegerValue(((Number) value).intValue());
                }
                break;
            case DOUBLE:
                if (value instanceof Double) {
                    setDoubleValue((Double) value);
                } else if (value instanceof Number) {
                    setDoubleValue(((Number) value).doubleValue());
                }
                break;
            case STRING:
                if (value instanceof String) {
                    setStringValue((String) value);
                } else if (value != null) {
                    setStringValue(value.toString());
                }
                break;
            case ENUM:
                if (value instanceof Enum<?>) {
                    setEnumValue((Enum<?>) value);
                }
                break;
            case SET:
                if (value instanceof Set<?>) {
                    setSetValue((Set<?>) value);
                }
                break;
        }
    }

    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    public int getIntValue() {
        if (type == SettingType.INTEGER) {
            return (int) value;
        }
        throw new IllegalStateException("Setting is not an integer");
    }

    public Number getMinValue() {
        return minValue;
    }

    public Number getMaxValue() {
        return maxValue;
    }

    public boolean hasRange() {
        return minValue != null && maxValue != null;
    }
}