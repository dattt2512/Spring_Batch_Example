package com.tdt.springbatch.controller;

import com.tdt.springbatch.service.OrderService;
import com.tdt.springbatch.thread.SimpleThread;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;

@RestController
@RequestMapping("api")
public class OrderController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private OrderService orderService;

    @PostMapping("insert/{recordsTotal}")
    public String batchInsert(@PathVariable(name = "recordsTotal") int recordsTotal) throws BrokenBarrierException, InterruptedException {

        SimpleThread thread1 = new SimpleThread("thread1", 0, recordsTotal/10, orderService);
        SimpleThread thread2 = new SimpleThread("thread2", recordsTotal/10, recordsTotal/10*2, orderService);
        SimpleThread thread3 = new SimpleThread("thread3", recordsTotal/10*2, recordsTotal/10*3, orderService);
        SimpleThread thread4 = new SimpleThread("thread4", recordsTotal/10*3, recordsTotal/10*4, orderService);
        SimpleThread thread5 = new SimpleThread("thread5", recordsTotal/10*4, recordsTotal/10*5, orderService);
        SimpleThread thread6 = new SimpleThread("thread6", recordsTotal/10*5, recordsTotal/10*6, orderService);
        SimpleThread thread7 = new SimpleThread("thread7", recordsTotal/10*6, recordsTotal/10*7, orderService);
        SimpleThread thread8 = new SimpleThread("thread8", recordsTotal/10*7, recordsTotal/10*8, orderService);
        SimpleThread thread9 = new SimpleThread("thread9", recordsTotal/10*8, recordsTotal/10*9, orderService);
        SimpleThread thread10 = new SimpleThread("thread10", recordsTotal/10*9, recordsTotal, orderService);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();
        thread8.start();
        thread9.start();
        thread10.start();
        return "Done!";
    }

    @PostMapping("/exportCsv")
    public void exportDBToCsv() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();

        try {
            jobLauncher.run(job, jobParameters);

            File sourceFile = new File("tempOrders.csv");
            OutputStream os = new FileOutputStream(sourceFile);
            for (int i = 1; i < 9; i++) {
                File file = new File("orders" + i + ".csv");
                InputStream is = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                File file1 = new File("orders" + i);
                file1.delete();
            }

            String titleTransactionBy = "Transactions Report";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss");
            var dateTimeNow = LocalDateTime.now().format(formatter);
            var fileName = titleTransactionBy.replace(" ", "") + dateTimeNow;

            File reportCSV = new File(fileName + ".csv");

            orderService.copyFile(sourceFile, reportCSV);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/export")
    public void exportToPdf(@RequestParam("exportType") String exportType) throws JRException {

        String titleTransactionBy = "Transactions Report";

        String[] columnNames = new String[] {"id", "code", "stockCode", "price", "quantity", "total", "orderDate"};
        JRCsvDataSource jrCsvDataSource = new JRCsvDataSource("/home/eco1027_dattt/workspace/spring-batch/tempOrders.csv");
        jrCsvDataSource.setColumnNames(columnNames);
        jrCsvDataSource.setFieldDelimiter(';');
        jrCsvDataSource.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        HashMap<String, Object> parameters = new HashMap();
        parameters.put("title", titleTransactionBy);

        InputStream transactionReportStream =
                getClass()
                        .getResourceAsStream(
                                "/transaction_report_" + exportType.toString().toLowerCase() + ".jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(transactionReportStream);

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(jasperReport, parameters, jrCsvDataSource);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss");
        var dateTimeNow = LocalDateTime.now().format(formatter);
        var fileName = titleTransactionBy.replace(" ", "") + dateTimeNow;

        if (exportType.equalsIgnoreCase("pdf")) {

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName + ".pdf"));
            exporter.exportReport();

        } else if (exportType.equalsIgnoreCase("excel")) {

            JRXlsxExporter exporter = new JRXlsxExporter();
            SimpleXlsxReportConfiguration reportConfigXLS = new SimpleXlsxReportConfiguration();
            reportConfigXLS.setDetectCellType(true);
            reportConfigXLS.setCollapseRowSpan(false);
            exporter.setConfiguration(reportConfigXLS);
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName + ".xlsx"));
            exporter.exportReport();

        } else {
            throw new RuntimeException("Export Type doesn't supported.");
        }
    }
}
