package sample.acceptance.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Do not run these tests as part of an IDE build or individually.
 * These are acceptance tests for the spring cloud stream samples.
 * The recommended way to run these tests are using the runAcceptanceTests.sh script in this module.
 * More about running that script can be found in the README.
 *
 * @author Soby Chacko
 */
public class SampleAcceptanceTests {

	private static final Logger logger = LoggerFactory.getLogger(SampleAcceptanceTests.class);

	@Test
	public void testJdbcSourceSampleKafka() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-source-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("JDBC Source", "Started SampleJdbcSource in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			boolean foundAssertionStrings = waitForLogEntry("JDBC Source", "Data received...[{id=1, name=Bob, tag=null}, {id=2, name=Jane, tag=null}, {id=3, name=John, tag=null}]");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testJdbcSourceSampleRabbit() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-source-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("JDBC Source", "Started SampleJdbcSource in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			boolean foundAssertionStrings = waitForLogEntry("JDBC Source", "Data received...[{id=1, name=Bob, tag=null}, {id=2, name=Jane, tag=null}, {id=3, name=John, tag=null}]");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testJdbcSinkSampleKafka() throws Exception {

		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-sink-kafka-sample.jar", "--logging.file=/tmp/foobarx.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("JDBC Sink", "Started SampleJdbcSink in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}

			JdbcTemplate db;
			DataSource dataSource = new SingleConnectionDataSource("jdbc:mariadb://localhost:3306/sample_mysql_db",
					"root", "pwd", false);

			db = new JdbcTemplate(dataSource);

			long timeout = System.currentTimeMillis() + (30 * 1000);
			boolean exists = false;
			while (!exists && System.currentTimeMillis() < timeout) {
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException(e.getMessage(), e);
				}

				Integer count = db.queryForObject("select count(*) from test", Integer.class);

				if (count > 0) {
					exists = true;
				}
			}
			if (!exists) {
				fail("No records found in database!");
			}

		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testJdbcSinkSampleRabbit() throws Exception {

		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-sink-rabbit-sample.jar", "--logging.file=/tmp/foobarx.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("JDBC Sink", "Started SampleJdbcSink in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}

			JdbcTemplate db;
			DataSource dataSource = new SingleConnectionDataSource("jdbc:mariadb://localhost:3306/sample_mysql_db",
					"root", "pwd", false);

			db = new JdbcTemplate(dataSource);

			long timeout = System.currentTimeMillis() + (30 * 1000);
			boolean exists = false;
			while (!exists && System.currentTimeMillis() < timeout) {
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException(e.getMessage(), e);
				}

				Integer count = db.queryForObject("select count(*) from test", Integer.class);

				if (count > 0) {
					exists = true;
				}
			}
			if (!exists) {
				fail("No records found in database!");
			}

		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testDynamicSourceSampleKafka() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/dynamic-destination-source-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("JDBC Source", "Started SourceApplication in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForObject(
					"http://localhost:8080",
					"{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}", String.class);
			boolean foundAssertionStrings = waitForLogEntry("JDBC Source", "Data received from customer-1...{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
			restTemplate.postForObject(
					"http://localhost:8080",
					"{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}", String.class);
			foundAssertionStrings = waitForLogEntry("JDBC Source", "Data received from customer-2...{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testDynamicSourceSampleRabbit() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/dynamic-destination-source-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("JDBC Source", "Started SourceApplication in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForObject(
					"http://localhost:8080",
					"{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}", String.class);
			boolean foundAssertionStrings = waitForLogEntry("JDBC Source", "Data received from customer-1...{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
			restTemplate.postForObject(
					"http://localhost:8080",
					"{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}", String.class);
			foundAssertionStrings = waitForLogEntry("JDBC Source", "Data received from customer-2...{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testMultiBinderKafkaInputRabbitOutput() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/multibinder-kafka-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*");

			p = pb.start();
			boolean started = waitForLogEntry("Multibinder", "Started MultibinderApplication in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			boolean foundAssertionStrings = waitForLogEntry("Multibinder", "Data received...bar", "Data received...foo");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testMultiBinderTwoKafkaClusters() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/multibinder-two-kafka-clusters-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*",
					"--kafkaBroker1=localhost:9092", "--zk1=localhost:2181",
					"--kafkaBroker2=localhost:9093", "--zk2=localhost:2182");

			p = pb.start();
			boolean started = waitForLogEntry("Multibinder 2 Kafka Clusters", "Started MultibinderApplication in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			boolean foundAssertionStrings = waitForLogEntry("Multibinder 2 Kafka Clusters", "Data received...bar", "Data received...foo");
			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}

	@Test
	public void testKafkaStreamsWordCount() throws Exception {
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/kafka-streams-word-count-sample.jar", "--logging.file=/tmp/foobar.log",
					"--management.endpoints.web.exposure.include=*",
					"--spring.cloud.stream.kafka.streams.timeWindow.length=60000");

			p = pb.start();
			boolean started = waitForLogEntry("Kafka Streams WordCount", "Started KafkaStreamsWordCountApplication in");
			if (!started) {
				fail("process didn't start in 30 seconds");
			}
			boolean foundAssertionStrings = waitForLogEntry("Kafka Streams WordCount",
					"Data received...{\"word\":\"foo\",\"count\":1,",
					"Data received...{\"word\":\"bar\",\"count\":1,",
					"Data received...{\"word\":\"foobar\",\"count\":1,",
					"Data received...{\"word\":\"baz\",\"count\":1,",
					"Data received...{\"word\":\"fox\",\"count\":1,");

			if (!foundAssertionStrings) {
				fail("Did not find the text looking for after waiting for 30 seconds");
			}
		} finally {
			if (p != null) {
				p.destroyForcibly();
			}
		}
	}


	boolean waitForLogEntry(String app, String... entries) {
		logger.info("Looking for '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
		long timeout = System.currentTimeMillis() + (30 * 1000);
		boolean exists = false;
		while (!exists && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(7 * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e.getMessage(), e);
			}
			if (!exists) {
				logger.info("Polling to get log file. Remaining poll time = "
						+ (timeout - System.currentTimeMillis() + " ms."));
				String log = getLog("http://localhost:8080/actuator");
				if (log != null) {
					if (Stream.of(entries).allMatch(s -> log.contains(s))) {
						exists = true;
					}
				}
			}
		}
		if (exists) {
			logger.info("Matched all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for app " + app);
		} else {
			logger.error("ERROR: Couldn't find all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
		}
		return exists;
	}

	String getLog(String url) {
		RestTemplate restTemplate = new RestTemplate();
		String logFileUrl = String.format("%s/logfile", url);
		String log = null;
		try {
			log = restTemplate.getForObject(logFileUrl, String.class);
			if (log == null) {
				logger.info("Unable to retrieve logfile from '" + logFileUrl);
			} else {
				logger.info("Retrieved logfile from '" + logFileUrl);
			}
		} catch (HttpClientErrorException e) {
			logger.info("Failed to access logfile from '" + logFileUrl + "' due to : " + e.getMessage());
		} catch (Exception e) {
			logger.warn("Error while trying to access logfile from '" + logFileUrl + "' due to : " + e);
		}
		return log;
	}

}
