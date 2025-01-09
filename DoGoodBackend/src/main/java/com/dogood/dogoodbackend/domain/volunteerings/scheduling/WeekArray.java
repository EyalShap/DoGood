package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.Arrays;

public class WeekArray implements UserType<boolean[]> {

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<boolean[]> returnedClass() {
        return boolean[].class;
    }

    @Override
    public boolean equals(boolean[] booleans, boolean[] j1) {
        return Arrays.equals(booleans, j1);
    }

    @Override
    public int hashCode(boolean[] booleans) {
        return Arrays.hashCode(booleans);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, boolean[] booleans, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
        if (preparedStatement != null) {
            if (booleans != null) {
                Array array = sharedSessionContractImplementor.getJdbcConnectionAccess().obtainConnection()
                        .createArrayOf("bool", toObjectArray(booleans));
                preparedStatement.setArray(i, array);
            } else {
                preparedStatement.setNull(i, Types.ARRAY);
            }
        }
    }

    @Override
    public boolean[] deepCopy(boolean[] booleans) {
        if(booleans == null){
            return null;
        }
        return Arrays.copyOf(booleans, booleans.length);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(boolean[] booleans) {
        return booleans;
    }

    @Override
    public boolean[] assemble(Serializable serializable, Object o) {
        return (boolean[]) serializable;
    }

    private Boolean[] toObjectArray(boolean[] primitiveArray) {
        Boolean[] objectArray = new Boolean[primitiveArray.length];
        for (int i = 0; i < primitiveArray.length; i++) {
            objectArray[i] = primitiveArray[i];
        }
        return objectArray;
    }
}
