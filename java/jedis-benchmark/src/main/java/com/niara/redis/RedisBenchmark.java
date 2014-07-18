package com.niara.redis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

//import redis.clients.jedis.HostAndPort;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
//import redis.clients.jedis.tests.HostAndPortUtil;

public class RedisBenchmark {
  //private static HostAndPort hnp = HostAndPort();
  private static final int TOTAL_OPERATIONS = 100000;

  static String readResource(String path) throws IOException {
    //InputStream IS = this.getClass().getResourceAsStream("MyData.txt");
    URL url = Resources.getResource(path);
    String text = Resources.toString(url, Charsets.UTF_8);
    return text;
  }

  public static void main(String[] args) throws UnknownHostException, IOException {
    /*
      Pipelined example
     */
    Jedis jedis;
    jedis = new Jedis("localhost");
    jedis.connect();
    jedis.flushAll();
    long begin = Calendar.getInstance().getTimeInMillis();
    Pipeline p = jedis.pipelined();
    for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
      String key = "foo" + n;
      p.set(key, "bar" + n);
      p.get(key);
    }
    p.sync();
    long elapsed = Calendar.getInstance().getTimeInMillis() - begin;
    jedis.disconnect();
    System.out.println("In pipelined example with " + TOTAL_OPERATIONS + " operations, achieved " +
            ((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " operations/sec");

    /*
      Non-pipelined example
     */
    jedis.connect();
    jedis.flushAll();
    begin = Calendar.getInstance().getTimeInMillis();
    for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
      String key = "foo" + n;
      jedis.set(key, "bar" + n);
      jedis.get(key);
    }
    elapsed = Calendar.getInstance().getTimeInMillis() - begin;
    jedis.disconnect();
    System.out.println("In non-pipelined example with " + TOTAL_OPERATIONS + " operations, achieved " +
            ((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " operations/sec");

    /*
      Scripting example
     */
    jedis.connect();
    jedis.flushAll();
    begin = Calendar.getInstance().getTimeInMillis();
    String script = readResource("scripts/CheckAndSet.lua");
    String sha1 = jedis.scriptLoad(script);
    String key = "myint";
    List<String> skeys = Arrays.asList(key);
    Random randomGenerator = new Random();
    int randomInt;
    randomInt = randomGenerator.nextInt(TOTAL_OPERATIONS);
    jedis.set(key, String.valueOf(randomInt));
    for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
      randomInt = randomGenerator.nextInt(TOTAL_OPERATIONS);
      List<String> sargs = Arrays.asList(String.valueOf(randomInt));
      String response = (String) jedis.evalsha(sha1, skeys, sargs);
      if (!response.equals("OK") && !response.equals("NOT SET")) {
        System.out.println("Unexpected response: " + response);
      }
    }
    elapsed = Calendar.getInstance().getTimeInMillis() - begin;
    jedis.disconnect();
    System.out.println("In scripting example with " + TOTAL_OPERATIONS + " operations, achieved " +
            ((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " operations/sec");

  }

}