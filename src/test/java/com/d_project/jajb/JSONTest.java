package com.d_project.jajb;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSONTest
 * @author Kazuhiko Arase
 */
public class JSONTest {

  protected final Logger logger = LoggerFactory.getLogger(getClass() );

  protected interface ObjectHandler {
    Object handle(Object obj);
  }

  protected void test(String src) throws Exception{
    test(src, src);
  }

  protected void test(String src, String expected) throws Exception {
    String actual = JSON.stringify(JSON.parse(src) );
    Assert.assertEquals(expected, actual);
  }

  protected void test(String src,
      Class<?> targetClass,
      ObjectHandler h) throws Exception{
    test(src, src, targetClass, h);
  }

  protected void test(String src, String expected,
      Class<?> targetClass,
      ObjectHandler h) throws Exception {
    String actual = JSON.stringify(h.handle(JSON.parse(src, targetClass) ) );
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testPlain() throws Exception {

    test("null");
    test(" null ", "null");
    test("true");
    test("false");
    test("NaN");
    test("+1E2", "1E+2");
    test("-1E2", "-1E+2");
    test("\"\"","\"\"");
    test("\"a\"","\"a\"");
    test(" \"a\"","\"a\"");
    test("\"\\u3000\"","\"\u3000\"");

  }

  @Test
  public void testObjects() throws Exception {

    test("[1,20,-3]");
    test(" [01, 020 ,-03] ","[1,20,-3]");
    test("\"\\u3000\"","\"\u3000\"");
    test("[0.5,-0.5,0.3]");
    test("[\"1\",\"2\",\"3\"]");
    test("[\"\\b\\f\\n\\t\\r\"]");

  }

  @Test
  public void testComplex() throws Exception {
    test("[\"\\n\\t\\r\",[\"1\",\"2\",\"3\"]]");
    test("[{},{},{}]");
    test("[1,2,[3],4,[5,6],7]");
    test("{\"a\":[],\"c\":[],\"b\":[]}");
  }

  @Test
  public void testPOJO1() throws Exception {
    test("{\"group\":null,\"items\":null,\"num\":1,\"str\":\"1\"}",
        TestVO.class, new ObjectHandler() {
      @Override
      public Object handle(Object obj) {
        Assert.assertEquals(TestVO.class, obj.getClass() );
        TestVO vo = (TestVO)obj;
        Assert.assertEquals("1", vo.getStr() );
        Assert.assertEquals(1, vo.getNum() );
        Assert.assertNull(vo.getGroup() );
        Assert.assertNull(vo.getItems() );
        return obj;
      }
    });
  }

  @Test
  public void testPOJO2() throws Exception {
    test("{\"group\":{}," +
        "\"items\":[],\"num\":1,\"str\":\"1\"}",
        "{\"group\":{\"s1\":null,\"s2\":null}," +
        "\"items\":[],\"num\":1,\"str\":\"1\"}",
        TestVO.class, new ObjectHandler() {
      @Override
      public Object handle(Object obj) {
        Assert.assertEquals(TestVO.class, obj.getClass() );
        TestVO vo = (TestVO)obj;
        Assert.assertEquals("1", vo.getStr() );
        Assert.assertEquals(1, vo.getNum() );
        Assert.assertNotNull(vo.getGroup() );
        Assert.assertNotNull(vo.getItems() );
        return obj;
      }
    });
  }

  @Test
  public void testPOJO3() throws Exception {
    test("{\"group\":{\"s1\":\"a\",\"s2\":\"b\"}," +
        "\"items\":[{\"f1\":\"@\",\"f2\":2},{\"f1\":\"%\"}],\"num\":1,\"str\":\"1\"}",
        "{\"group\":{\"s1\":\"a\",\"s2\":\"b\"}," +
        "\"items\":[{\"f1\":\"@\"},{\"f1\":\"%\"}],\"num\":1,\"str\":\"1\"}",
        TestVO.class, new ObjectHandler() {
      @Override
      public Object handle(Object obj) {
        Assert.assertEquals(TestVO.class, obj.getClass() );
        TestVO vo = (TestVO)obj;
        Assert.assertEquals("1", vo.getStr() );
        Assert.assertEquals(1, vo.getNum() );
        Assert.assertNotNull(vo.getGroup() );
        Assert.assertEquals("a", vo.getGroup().getS1() );
        Assert.assertEquals("b", vo.getGroup().getS2() );
        Assert.assertNotNull(vo.getItems() );
        Assert.assertEquals(2, vo.getItems().size() );
        Assert.assertEquals("@", vo.getItems().get(0).getF1() );
        Assert.assertEquals("%", vo.getItems().get(1).getF1() );
        return obj;
      }
    });
  }

  @Test
  public void testPOJO4() throws Exception {
    test("{\"arr\":[1,2,3],\"flg\":true,\"group\":null," +
        "\"items\":null,\"num\":1,\"str\":\"1\"}",
        TestVO4.class, new ObjectHandler() {
      @Override
      public Object handle(Object obj) {
        Assert.assertEquals(TestVO4.class, obj.getClass() );
        TestVO4 vo = (TestVO4)obj;
        Assert.assertEquals("1", vo.getStr() );
        Assert.assertEquals(1, vo.getNum() );
        Assert.assertTrue(vo.isFlg() );
        Assert.assertNull(vo.getGroup() );
        Assert.assertNull(vo.getItems() );
        Assert.assertEquals(3, vo.getArr().length);
        Assert.assertEquals(1, vo.getArr()[0]);
        Assert.assertEquals(2, vo.getArr()[1]);
        Assert.assertEquals(3, vo.getArr()[2]);
        return obj;
      }
    });
  }

  @Test
  public void testPOJO5() throws Exception {
    Assert.assertEquals("[1,2]",JSON.stringify(new byte[] {1,2}) );
    Assert.assertEquals("[1,2]",JSON.stringify(new short[] {1,2}) );
    Assert.assertEquals("[1,2]",JSON.stringify(new int[] {1,2}) );
    Assert.assertEquals("[1,2]",JSON.stringify(new long[] {1,2}) );
    Assert.assertEquals("[1.0,2.0]",JSON.stringify(new float[] {1,2}) );
    Assert.assertEquals("[1.0,2.0]",JSON.stringify(new double[] {1,2}) );
  }

  @Test
  public void testPOJO6() throws Exception {
    Assert.assertArrayEquals(new byte[] {1,2},
        JSON.parse("[1,2]", byte[].class) );
    Assert.assertArrayEquals(new int[] {1,2},
        JSON.parse("[1,2]", int[].class) );
    Assert.assertArrayEquals(new int[] {1,2},
        JSON.parse("[1,2]", int[].class) );
    Assert.assertArrayEquals(new long[] {1,2},
        JSON.parse("[1,2]", long[].class) );
    Assert.assertArrayEquals(new float[] {1.0f,2},
        JSON.parse("[1,2]", float[].class), 0);
    Assert.assertArrayEquals(new double[] {1,2.0},
        JSON.parse("[1,2]", double[].class), 0);
    Assert.assertArrayEquals(new BigInteger[] {
        BigInteger.valueOf(1), BigInteger.valueOf(2)},
        JSON.parse("[1,2]", BigInteger[].class) );
    Assert.assertArrayEquals(new BigDecimal[] {
        BigDecimal.valueOf(1), BigDecimal.valueOf(2)},
        JSON.parse("[1,2]", BigDecimal[].class) );
  }

}
