package org.diehl.spatium.presentation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.validation.Validation;
import javax.validation.Validator;

public class PresentationConfig {

    @Produces
    @ApplicationScoped
    public Validator getValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
