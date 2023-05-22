package com.tdt.springbatch.service;

import net.sf.jasperreports.engine.JRException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

//@EnableScheduling
public interface OrderService {
    int[] batchInsert(int recordsStart, int recordsStop);

    void exportDBToCsv();

    void copyFile(File source, File dest) throws IOException;

    void reportPdf() throws JRException;

    void reportExcel() throws JRException;
}
