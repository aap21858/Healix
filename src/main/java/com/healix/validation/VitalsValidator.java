package com.healix.validation;

import com.healix.model.VitalsRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for Vitals data to ensure medical accuracy and data integrity
 */
@Component
public class VitalsValidator {

    /**
     * Validate vitals data and return list of validation errors
     */
    public List<String> validate(VitalsRequest request) {
        List<String> errors = new ArrayList<>();

        // Validate weight
        if (request.getWeight() != null) {
            if (request.getWeight() <= 0) {
                errors.add("Weight must be greater than 0 kg");
            } else if (request.getWeight() > 500) {
                errors.add("Weight cannot exceed 500 kg");
            }
        }

        // Validate height
        if (request.getHeight() != null) {
            if (request.getHeight() < 10) {
                errors.add("Height must be at least 10 cm");
            } else if (request.getHeight() > 300) {
                errors.add("Height cannot exceed 300 cm");
            }
        }

        // Validate head circumference
        if (request.getHeadCircumference() != null) {
            if (request.getHeadCircumference() < 10) {
                errors.add("Head circumference must be at least 10 cm");
            } else if (request.getHeadCircumference() > 100) {
                errors.add("Head circumference cannot exceed 100 cm");
            }
        }

        // Validate temperature
        if (request.getTemperature() != null) {
            String unit = request.getTemperatureUnit() != null ?
                request.getTemperatureUnit().getValue() : "F";

            if ("F".equals(unit)) {
                if (request.getTemperature() < 77) {
                    errors.add("Temperature in Fahrenheit must be at least 77°F");
                } else if (request.getTemperature() > 115) {
                    errors.add("Temperature in Fahrenheit cannot exceed 115°F");
                }
            } else if ("C".equals(unit)) {
                if (request.getTemperature() < 25) {
                    errors.add("Temperature in Celsius must be at least 25°C");
                } else if (request.getTemperature() > 46) {
                    errors.add("Temperature in Celsius cannot exceed 46°C");
                }
            }
        }

        // Validate heart rate
        if (request.getHeartRate() != null) {
            if (request.getHeartRate() < 20) {
                errors.add("Heart rate must be at least 20 bpm");
            } else if (request.getHeartRate() > 300) {
                errors.add("Heart rate cannot exceed 300 bpm");
            }
            // Note: Abnormal ranges (< 40 or > 200) are handled in generateWarnings()
        }

        // Validate respiratory rate
        if (request.getRespiratoryRate() != null) {
            if (request.getRespiratoryRate() < 1) {
                errors.add("Respiratory rate must be at least 1 breath/min");
            } else if (request.getRespiratoryRate() > 100) {
                errors.add("Respiratory rate cannot exceed 100 breaths/min");
            }
        }

        // Validate blood pressure
        if (request.getSystolicBp() != null) {
            if (request.getSystolicBp() < 40) {
                errors.add("Systolic BP must be at least 40 mmHg");
            } else if (request.getSystolicBp() > 300) {
                errors.add("Systolic BP cannot exceed 300 mmHg");
            }
        }

        if (request.getDiastolicBp() != null) {
            if (request.getDiastolicBp() < 20) {
                errors.add("Diastolic BP must be at least 20 mmHg");
            } else if (request.getDiastolicBp() > 200) {
                errors.add("Diastolic BP cannot exceed 200 mmHg");
            }
        }

        // Cross-field validation: systolic should be greater than diastolic
        if (request.getSystolicBp() != null && request.getDiastolicBp() != null) {
            if (request.getSystolicBp() <= request.getDiastolicBp()) {
                errors.add("Systolic BP must be greater than Diastolic BP");
            }
        }

        // Validate SpO2
        if (request.getSpo2() != null) {
            if (request.getSpo2() < 0) {
                errors.add("SpO2 cannot be negative");
            } else if (request.getSpo2() > 100) {
                errors.add("SpO2 cannot exceed 100%");
            }
        }

        // Validate blood sugar
        if (request.getRandomBloodSugar() != null) {
            if (request.getRandomBloodSugar() < 0) {
                errors.add("Blood sugar cannot be negative");
            } else if (request.getRandomBloodSugar() > 1000) {
                errors.add("Blood sugar value seems unrealistic (>1000 mg/dL)");
            }
        }

        // Validate pain level
        if (request.getPainLevel() != null) {
            if (request.getPainLevel() < 0 || request.getPainLevel() > 10) {
                errors.add("Pain level must be between 0 and 10");
            }
        }

        // Validate bmiStatus (optional) - if provided, ensure it's one of expected values
        if (request.getBmiStatus() != null) {
            // It's an enum in the generated model; presence implies valid value, but keep a defensive check
            try {
                String v = request.getBmiStatus().toString();
                if (!("Underweight".equals(v) || "Normal".equals(v) || "Overweight".equals(v) || "Obese".equals(v))) {
                    errors.add("BMI status must be one of: Underweight, Normal, Overweight, Obese");
                }
            } catch (Exception e) {
                errors.add("Invalid BMI status value");
            }
        }

        // Validate that at least one vital sign is provided
        if (isAllFieldsNull(request)) {
            errors.add("At least one vital sign measurement must be provided");
        }

        return errors;
    }

    /**
     * Check if all vital sign fields are null
     */
    private boolean isAllFieldsNull(VitalsRequest request) {
        return request.getWeight() == null &&
               request.getHeight() == null &&
               request.getHeadCircumference() == null &&
               request.getTemperature() == null &&
               request.getHeartRate() == null &&
               request.getRespiratoryRate() == null &&
               request.getSystolicBp() == null &&
               request.getDiastolicBp() == null &&
               request.getSpo2() == null &&
               request.getRandomBloodSugar() == null &&
               request.getPainLevel() == null &&
               (request.getSymptoms() == null || request.getSymptoms().isEmpty()) &&
               request.getBmiStatus() == null; // consider bmiStatus as a valid provided field
    }

    /**
     * Generate warning messages for vital signs that are within valid range but potentially concerning
     */
    public List<String> generateWarnings(VitalsRequest request) {
        List<String> warnings = new ArrayList<>();

        // Heart rate warnings
        if (request.getHeartRate() != null) {
            if (request.getHeartRate() < 50) {
                warnings.add("Heart rate is below normal range (<50 bpm - Bradycardia)");
            } else if (request.getHeartRate() > 120) {
                warnings.add("Heart rate is above normal range (>120 bpm - Tachycardia)");
            }
        }

        // Blood pressure warnings
        if (request.getSystolicBp() != null && request.getDiastolicBp() != null) {
            if (request.getSystolicBp() < 90 || request.getDiastolicBp() < 60) {
                warnings.add("Blood pressure is low (Hypotension)");
            } else if (request.getSystolicBp() >= 140 || request.getDiastolicBp() >= 90) {
                warnings.add("Blood pressure is high (Hypertension)");
            }
        }

        // SpO2 warnings
        if (request.getSpo2() != null && request.getSpo2() < 95) {
            warnings.add("Oxygen saturation is below normal (<95%)");
        }

        // Temperature warnings
        if (request.getTemperature() != null) {
            String unit = request.getTemperatureUnit() != null ?
                request.getTemperatureUnit().getValue() : "F";

            if ("F".equals(unit)) {
                if (request.getTemperature() < 97) {
                    warnings.add("Temperature is below normal (Hypothermia)");
                } else if (request.getTemperature() >= 100.4) {
                    warnings.add("Temperature indicates fever");
                }
            } else if ("C".equals(unit)) {
                if (request.getTemperature() < 36.1) {
                    warnings.add("Temperature is below normal (Hypothermia)");
                } else if (request.getTemperature() >= 38) {
                    warnings.add("Temperature indicates fever");
                }
            }
        }

        // Respiratory rate warnings
        if (request.getRespiratoryRate() != null) {
            if (request.getRespiratoryRate() < 12) {
                warnings.add("Respiratory rate is below normal range (<12 breaths/min)");
            } else if (request.getRespiratoryRate() > 20) {
                warnings.add("Respiratory rate is above normal range (>20 breaths/min)");
            }
        }

        // Blood sugar warnings
        if (request.getRandomBloodSugar() != null) {
            if (request.getRandomBloodSugar() < 70) {
                warnings.add("Blood sugar is low (Hypoglycemia)");
            } else if (request.getRandomBloodSugar() > 200) {
                warnings.add("Blood sugar is high (Hyperglycemia)");
            }
        }

        return warnings;
    }
}
