package com.niara.redis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Calendar;

//import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
//import redis.clients.jedis.tests.HostAndPortUtil;

public class RedisBenchmark {
  //private static HostAndPort hnp = HostAndPort();
  private static final int TOTAL_OPERATIONS = 200000;

  public static void main(String[] args) throws UnknownHostException, IOException {
    /*
      Pipelined example
     */
    Jedis jedis = new Jedis("localhost");
    jedis.connect();
    //jedis.auth("foobared");
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
    System.out.println("In pipelined example with " + TOTAL_OPERATIONS + " operations, acheived " +
            ((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " operations/sec");

    /*
      Non-pipelined example
     */
    jedis.connect();
    //jedis.auth("foobared");
    jedis.flushAll();
    begin = Calendar.getInstance().getTimeInMillis();
    for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
      String key = "foo" + n;
      jedis.set(key, "bar" + n);
      jedis.get(key);
    }
    elapsed = Calendar.getInstance().getTimeInMillis() - begin;
    jedis.disconnect();
    System.out.println("In non-pipelined example with " + TOTAL_OPERATIONS + " operations, acheived " +
            ((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " operations/sec");
  }
}