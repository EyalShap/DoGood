package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Embeddable
public class BarcodeHandler {
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Code> recentCodes;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Code> constantCodes;
    private transient final int SECONDS_UNTIL_INVALID = 15;

    public BarcodeHandler() {
        this.recentCodes = new LinkedList<>();
        this.constantCodes = new LinkedList<>();
    }

    public List<Code> getRecentCodes() {
        cleanCodes();
        return recentCodes;
    }

    public List<Code> getConstantCodes() {
        return constantCodes;
    }

    public boolean codeValid(String code){
        cleanCodes();
        for(Code c : recentCodes){
            if(c.getCode().equals(code)){
                return true;
            }
        }
        for(Code c : constantCodes){
            if(c.getCode().equals(code)){
                return true;
            }
        }
        return false;
    }

    public String generateCode(){
        cleanCodes();
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString();
        Code c = new Code(code, System.currentTimeMillis());
        recentCodes.add(c);
        return code;
    }

    public String generateConstantCode(){
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString();
        Code c = new Code(code, System.currentTimeMillis());
        constantCodes.add(c);
        return code;
    }

    public void clearConstantCodes(){
        this.constantCodes = new LinkedList<>();
    }

    private void cleanCodes(){
        synchronized (this.recentCodes) {
            List<Code> validCodes = new LinkedList<>();
            for(Code c : recentCodes){
                if(System.currentTimeMillis() - c.getCreated() <= SECONDS_UNTIL_INVALID*1000){
                    validCodes.add(c);
                }
            }
            this.recentCodes = validCodes;
        }
    }
}
