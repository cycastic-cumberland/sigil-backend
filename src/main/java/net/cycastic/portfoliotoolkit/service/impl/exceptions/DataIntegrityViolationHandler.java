package net.cycastic.portfoliotoolkit.service.impl.exceptions;

import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.ExceptionConvertor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;

@Component
public class DataIntegrityViolationHandler implements ExceptionConvertor {
    @Override
    public void tryConvert(Throwable t) {
        if (!(t instanceof SQLIntegrityConstraintViolationException ||
                t instanceof DataIntegrityViolationException)){
            return;
        }

        throw new RequestException(409, t, "Data conflict detected");
    }
}
