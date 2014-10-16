package org.monroe.team.chekit.services;

import org.junit.Assert;
import org.junit.Test;
import org.monroe.team.chekit.common.Closure;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CheckSuitParserTest {

    CheckSuitParser testInstance = new CheckSuitParser();

    @Test
    public void shouldParseWithNoExpceptions() throws IOException, CheckSuitParser.FormatException {
       CheckSuitParser.CheckItDocument document = testInstance.parseImpl(new File("src/sample/sample.checkit"));
        System.out.println(document);
    }

    @Test
    public void shouldMatchTitle(){
        assertExecuted("[Title] Hello world  ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_TITLE, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        assertEquals("Hello world", strings.get(0).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchComment(){
        assertExecuted("  #[Title] Hello world  ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_COMMENT, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldNotMatchComment(){
        assertNotExecuted("  [Ti#tle]  #Hello world  ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_COMMENT, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        System.out.println(strings);
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchCheck(){
        assertExecuted("         - this is a check", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_CHECK, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(9,strings.get(0).length());
                        Assert.assertEquals("this is a check",strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchCheckWithZeroSpaces(){
        assertExecuted("- this is a check ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_CHECK, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(0,strings.get(0).length());
                        Assert.assertEquals("this is a check",strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchCheckWithDouble(){
        assertExecuted("        - this - is a check ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_CHECK, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(8,strings.get(0).length());
                        Assert.assertEquals("this - is a check",strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchCheckWithRef(){
        assertExecuted("        - [refer here - #] ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_CHECK, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(8,strings.get(0).length());
                        Assert.assertEquals("[refer here - #]",strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldNotMatchAction(){
        assertNotExecuted("        - this - is a check ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_ACTION, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(8, strings.get(0).length());
                        Assert.assertEquals("this - is a check", strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldNotMatchActionEmptyLine(){
        assertNotExecuted("        ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_ACTION, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        System.out.println(strings);
                        Assert.assertEquals(8, strings.get(0).length());
                        Assert.assertEquals("this - is a check", strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }


    @Test
    public void shouldMatchAction(){
        assertExecuted("\t    User action 1", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_ACTION, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(8, strings.get(0).length());
                        Assert.assertEquals("User action 1", strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchActionWithNoSpaces(){
        assertExecuted("this - is a action ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_ACTION, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        Assert.assertEquals(0, strings.get(0).length());
                        Assert.assertEquals("this - is a action", strings.get(1).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchCommentEvenIfNothingThere(){
        assertExecuted("#", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_COMMENT, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchTitleWithTab(){
        assertExecuted("    [Title] Hello world  ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_TITLE, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        assertEquals("Hello world",strings.get(0).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchTitleWithSpaces(){
        assertExecuted("  [Title] Hello world  ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_TITLE, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        assertEquals("Hello world",strings.get(0).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldMatchGroupCaption(){
        assertExecuted("  [group_caption] ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_GROUP, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        assertEquals("group_caption",strings.get(0).trim());
                        return true;
                    }
                });
            }
        });
    }

    @Test
    public void shouldNotMatchGroupCaptionAndSkipComments(){
        assertNotExecuted("  [group_caption] #Comments could be plassed here", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_GROUP, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        assertEquals("group_caption", strings.get(0).trim());
                        return true;
                    }
                });
            }
        });
    }
    @Test
    public void shouldNotMatchEmptyTitle(){
        assertNotExecuted("  [Title]   ", new Closure<Boolean, String>() {
            @Override
            public Boolean call(String toTest) {
                return testInstance.extractAndDo(toTest, CheckSuitParser.ROW_TITLE, new Closure<Boolean, List<String>>() {
                    @Override
                    public Boolean call(List<String> strings) {
                        System.out.println(strings);
                        return true;
                    }
                });
            }
        });
    }


    public void assertExecuted(String testString, Closure<Boolean,String> toExecute){
       assertTrue(testString, toExecute.call(testString));
    }


    public void assertNotExecuted(String testString, Closure<Boolean,String> toExecute){
        assertTrue(testString, !toExecute.call(testString));
    }

}