package com.t4j.mobilenurse;

import java.util.List;

/**
 * Created by naoki on 2015/07/17.
 */
public class DiagnoseResponse {
    int status;

    public List<Diagnose> diagnoses;
    public class Diagnose {
        int runk;
        String condition;
        String score;
    }

}
