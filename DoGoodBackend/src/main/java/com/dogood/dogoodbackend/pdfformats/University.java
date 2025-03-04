package com.dogood.dogoodbackend.pdfformats;

public enum University {
    BGU("bgu.ac.il"),
    TECH("technion.ac.il"),
    HAIFA("haifa.ac.il"),
    ELSE("");

    public final String suffix;

    private University(String suffix) {
        this.suffix = suffix;
    }

    public static University getUniversity(String email) {
        for(University v : values()){
            if( email.endsWith(v.suffix)){
                return v;
            }
        }
        return ELSE;
    }
}
