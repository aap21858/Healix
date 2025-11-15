# Swagger/OpenAPI Specification - Modular Structure

This directory contains the modular OpenAPI 3.0 specification for the Healix Clinic Management API.

## 📁 Directory Structure

```
swagger/
├── main.yml                    # Main OpenAPI spec file (entry point)
├── paths/                      # API endpoint definitions
│   ├── auth.yml               # Authentication endpoints
│   ├── patients.yml           # Patient management endpoints
│   ├── insurance-schemes.yml  # Insurance scheme endpoints
│   ├── staff.yml              # Staff management endpoints
│   ├── dropdown-lookup.yml    # Dropdown lookup endpoints
│   └── appointments.yml       # Appointment management endpoints
├── schemas/                    # Data model definitions
│   ├── common.yml             # Common schemas (enums, errors, pagination)
│   ├── auth.yml               # Authentication models
│   ├── patient.yml            # Patient-related models
│   ├── staff.yml              # Staff models
│   ├── dropdown.yml           # Dropdown lookup models
│   └── appointment.yml        # Appointment management models
├── parameters/                 # Reusable parameters
│   └── common.yml             # Common parameters (pagination, sorting)
└── responses/                  # Reusable responses
    └── common.yml             # Common responses (error responses)
```

## 🎯 Purpose

The specification has been split into multiple files to improve:
- **Readability**: Each file focuses on a specific domain
- **Maintainability**: Changes are easier to locate and manage
- **Reusability**: Common components can be referenced across files
- **Collaboration**: Multiple developers can work on different files simultaneously

## 📝 File Descriptions

### Main File
- **main.yml**: Entry point that references all other files. Contains API metadata, servers, security schemes, and path/schema references.

### Paths Files
Each file contains endpoint definitions for a specific domain:
- **auth.yml**: Login, token validation, password management
- **patients.yml**: Patient CRUD, search, CSV upload/download, activation
- **insurance-schemes.yml**: Insurance scheme lookup
- **staff.yml**: Staff management (CRUD operations)
- **dropdown-lookup.yml**: Generic dropdown/lookup data management
- **appointments.yml**: Complete appointment workflow including triage, vitals, examination, prescriptions, investigations, referrals, IPD admission/discharge

### Schema Files
Data model definitions organized by domain:
- **common.yml**: Shared enums (Gender, BloodGroup, etc.), error models, pagination
- **auth.yml**: Login/authentication request/response models
- **patient.yml**: Patient, emergency contact, insurance, medical history models
- **staff.yml**: Staff registration, update, response models
- **dropdown.yml**: Dropdown lookup request/response models
- **appointment.yml**: All appointment-related models (appointments, triage, vitals, examination, prescriptions, investigations, etc.)

### Parameters & Responses
Reusable components:
- **parameters/common.yml**: Pagination parameters (page, size, sort)
- **responses/common.yml**: Common error responses

## 🔧 How It Works

The OpenAPI Generator Maven Plugin reads the `main.yml` file and automatically resolves all `$ref` references to generate:
- API interfaces in `com.healix.api` package
- Model classes in `com.healix.model` package

### Maven Configuration
```xml
<inputSpec>${project.basedir}/src/main/resources/swagger/main.yml</inputSpec>
```

## 🚀 Usage

### Generate API Code
```bash
mvn clean compile
```

This will:
1. Read `swagger/main.yml`
2. Resolve all file references
3. Generate Java interfaces and models in `target/generated-sources/openapi`

### View API Documentation
Run the application and navigate to:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## ✏️ Making Changes

### Adding a New Endpoint
1. Add the endpoint definition in the appropriate `paths/*.yml` file
2. Add request/response models in the appropriate `schemas/*.yml` file
3. Reference the path in `main.yml` under the `paths` section
4. Reference the schemas in `main.yml` under `components.schemas`
5. Run `mvn compile` to regenerate the code

### Example: Adding a New Endpoint
**Step 1**: Add path in `paths/patients.yml`
```yaml
patients-export:
  get:
    tags:
      - Patient Management
    summary: Export patients
    operationId: exportPatients
    # ... rest of the definition
```

**Step 2**: Reference in `main.yml`
```yaml
paths:
  /api/patients/export:
    $ref: './paths/patients.yml#/patients-export'
```

**Step 3**: Regenerate code
```bash
mvn clean compile
```

## 📖 References

OpenAPI uses JSON references (`$ref`) to link files:
- Local file: `$ref: './paths/auth.yml#/login'`
- Within same file: `$ref: '#/LoginRequest'`
- Cross-file schema: `$ref: '../schemas/common.yml#/ErrorResponse'`

## 🔍 Benefits of Modular Structure

1. **Easier Navigation**: Find endpoints and models quickly
2. **Reduced Merge Conflicts**: Team members work on different files
3. **Better Organization**: Logical grouping by domain/feature
4. **Faster Loading**: IDEs handle smaller files better
5. **Reusability**: Common components referenced across files

## ⚠️ Important Notes

- Always use relative paths in `$ref` references
- Maintain consistent naming conventions across files
- Keep `main.yml` as the single source of truth for API metadata
- Test API generation after making changes: `mvn clean compile`
- **Important**: In `components.parameters` and `components.responses`, you must define each parameter/response individually with its own `$ref`, not reference the entire file directly

## 🔧 Common Issues

### Issue: SpecValidationException - $ref is not of type object

**Error Message:**
```
attribute components.responses.$ref is not of type `object`
```

**Cause:** Trying to reference an entire file at the component level

**Solution:** Define each parameter/response individually:

**❌ Wrong:**
```yaml
components:
  parameters:
    $ref: './parameters/common.yml'
  responses:
    $ref: './responses/common.yml'
```

**✅ Correct:**
```yaml
components:
  parameters:
    PageParam:
      $ref: './parameters/common.yml#/PageParam'
    SizeParam:
      $ref: './parameters/common.yml#/SizeParam'
  responses:
    UnauthorizedError:
      $ref: './responses/common.yml#/UnauthorizedError'
```

## 📚 Related Documentation

- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

**Note**: The old monolithic `swagger.yml` file can be safely removed once you verify that the modular structure works correctly.

