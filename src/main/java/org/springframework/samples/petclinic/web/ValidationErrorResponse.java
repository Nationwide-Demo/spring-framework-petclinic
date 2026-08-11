/*
 * Copyright 2002-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * JSON representation of the binding and validation errors of a request body, returned
 * instead of re-rendering a form.
 */
public record ValidationErrorResponse(String message, List<FieldValidationError> errors) {

    public record FieldValidationError(String field, String code, String message) {
    }

    public static ValidationErrorResponse from(BindingResult result) {
        List<FieldValidationError> errors = result.getFieldErrors().stream()
            .map(ValidationErrorResponse::toFieldValidationError)
            .toList();
        return new ValidationErrorResponse("Validation failed", errors);
    }

    private static FieldValidationError toFieldValidationError(FieldError error) {
        return new FieldValidationError(error.getField(), error.getCode(), error.getDefaultMessage());
    }

}
