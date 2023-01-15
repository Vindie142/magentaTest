package ru.kazberov.magentaTest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ru.kazberov.magentaTest.services.DualStream;

@SpringBootApplication
public class MagentaTestApplication {

	public static void main(String[] args) {
		// connecting logging to a file
		PrintStream out = null;
		PrintStream err = null;
		try {
			out = new PrintStream(new FileOutputStream("logs/" + LocalDate.now().toString().replaceAll(":","_") + "_out.log"));
			PrintStream dual = new DualStream(System.out, out);
		    System.setOut(dual);
		    err = new PrintStream(new FileOutputStream("logs/" + LocalDate.now().toString().replaceAll(":","_") + "_err.log"));
		    dual= new DualStream(System.err, err);
		    System.setErr(dual);
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			SpringApplication.run(MagentaTestApplication.class, args);
		    out.close();
		    err.close();
		}
	}
}
