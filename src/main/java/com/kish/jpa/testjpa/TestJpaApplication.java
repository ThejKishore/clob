package com.kish.jpa.testjpa;

import com.kish.jpa.testjpa.entity.Report;
import com.kish.jpa.testjpa.entity.ReportDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class TestJpaApplication {

    @Value("${upload-json}")
    String json;



    @Bean
    public CommandLineRunner commandLineRunner(ReportDao reportDao){
        return args ->{
            StopWatch stopWatch = new StopWatch();
            //insert the record.
            stopWatch.start("insert");
            long id = reportDao.save(Report.builder().content(json).name("patient-db").build());
            stopWatch.stop();

            //update the record multiple times.
            for (int i = 0; i < 200; i++) {
                stopWatch.start("Select "+i);
                var report =  reportDao.load(id);
                System.out.println("report.getContent() = " + report.getContent());
                stopWatch.stop();

                stopWatch.start("Update"+i);
                reportDao.update(id,json);
                stopWatch.stop();
            }
            System.out.println("stopWatch = " + stopWatch.prettyPrint());
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(TestJpaApplication.class, args);
    }

}
