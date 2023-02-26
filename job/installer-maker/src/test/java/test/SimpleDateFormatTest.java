package test;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class SimpleDateFormatTest {
	final static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	final static int poolSize = 50;
	final static int count = 5000;


	@Test(expected = ExecutionException.class)
	public void SimpleDateFormatIsNotSafeTest() throws InterruptedException, ExecutionException{
		// init
		ExecutorService exec = Executors.newFixedThreadPool(poolSize);
		List<Future<Date>> results = new ArrayList<Future<Date>>();

		try {
			// SimpleDateFormat을 이용한 parse 작업 (멀티 쓰레드)
			Callable<Date> task = makeNotThreadSafeTask();

			// Count만큼 수행
			for (int i=0; i<count; i++) {
				results.add(exec.submit(task));
			}

			// 결과 출력
			for (Future<Date> result : results) {
				System.out.println(result.get());
			}

		} catch (InterruptedException ie) {
			ie.printStackTrace();
			throw ie;
		} catch (ExecutionException ee) {
			ee.printStackTrace();
			throw ee;
		} finally {
			exec.shutdown();
		}
	}

	@Test
	public void SimpleDateFormatIsSafeTest() throws InterruptedException, ExecutionException{
		// init
		ExecutorService exec = Executors.newFixedThreadPool(poolSize);
		List<Future<Date>> results = new ArrayList<Future<Date>>();

		try {
			// SimpleDateFormat을 이용한 parse 작업 (멀티 쓰레드)
			Callable<Date> task = makeThreadSafeTask();

			// Count만큼 수행
			for (int i=0; i<count; i++) {
				results.add(exec.submit(task));
			}

			// 결과 출력
			for (Future<Date> result : results) {
				System.out.println(result.get());
			}

		} catch (InterruptedException ie) {
			ie.printStackTrace();
			throw ie;
		} catch (ExecutionException ee) {
			ee.printStackTrace();
			throw ee;
		} finally {
			exec.shutdown();
		}
	}


	private SimpleDateFormat newFormat() {
		return new SimpleDateFormat("yyyyMMdd");
	}

	private Callable<Date> makeNotThreadSafeTask(){
		Callable<Date> task = new Callable<Date>() {
			public Date call() throws Exception {
				String threadName = Thread.currentThread().getName();
				System.out.println("threadName ==> " + threadName);
				return format.parse("20150630");
			}
		};
		return task;
	}

	private Callable<Date> makeThreadSafeTask(){
		Callable<Date> task = new Callable<Date>() {
			public Date call() throws Exception {
				String threadName = Thread.currentThread().getName();
				System.out.println("threadName ==> " + threadName);
				return newFormat().parse("20150630"); // 정상 동작시키기 위해서는 이와 같이 사용해야 함
			}
		};
		return task;
	}

}