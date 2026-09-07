package com.schedulingservice.api.unit.infrastructure.web.exception;

import com.schedulingservice.api.domain.exception.DomainException;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.exception.ValidationException;
import com.schedulingservice.api.infrastructure.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Test
    void handlersShouldProduceProblemDetail() {
        final GlobalExceptionHandler handler = new GlobalExceptionHandler();

        final MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/test");
        final WebRequest webRequest = new ServletWebRequest(servletRequest);

        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "name", "invalid"));

        final MethodArgumentNotValidException manv = mock(MethodArgumentNotValidException.class);
        when(manv.getBindingResult()).thenReturn(bindingResult);
        when(manv.getMessage()).thenReturn("validation failed");

        final ProblemDetail invalid = handler.handleMethodArgumentNotValid(manv, webRequest);
        assertNotNull(invalid);
        assertEquals("Invalid Request", invalid.getTitle());

        final ProblemDetail notFound = handler.handleEntityNotFound(new EntityNotFoundException("Appointment", "id"), webRequest);
        assertNotNull(notFound);
        assertEquals("Entity Not Found", notFound.getTitle());

        final ProblemDetail validation = handler.handleValidationException(new ValidationException("field", "x", "bad"), webRequest);
        assertNotNull(validation);
        assertEquals("Validation Error", validation.getTitle());

        final ProblemDetail forbidden = handler.handleAccessDenied(new AccessDeniedException("denied"), webRequest);
        assertNotNull(forbidden);
        assertEquals("Forbidden", forbidden.getTitle());

        final ProblemDetail domain = handler.handleDomainException(new DomainException("domain failure"), webRequest);
        assertNotNull(domain);
        assertEquals("Domain Error", domain.getTitle());
    }
}
