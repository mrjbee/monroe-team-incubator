package org.monroe.team.chekit.services;

import org.junit.Assert;
import org.junit.Test;
import org.monroe.team.chekit.common.Closure;

import java.util.List;

public class TextRecognitionCoreTest {

    private final TextRecognitionCore testInstance = new TextRecognitionCore();

    @Test
    public void shouldExtractMarkStatus(){
      List<String> result = testInstance.extractUsing(" mark as passed", testInstance.extract_markAs_status_as_first);
      Assert.assertEquals(result.get(0),"passed");
    }

    @Test
    public void shouldExtractMarkStatusAndComment(){
        List<String> result = testInstance.extractUsing(" mark as passed and comment comments could be added inline",
                testInstance.extract_markAs_status_as_first_and_comment_as_second);
        Assert.assertEquals(result.get(0),"passed");
        Assert.assertEquals(result.get(1),"comments could be added inline");
    }


    @Test
    public void shouldExtractNonMarkStatus(){
        List<String> result = testInstance.extractUsing(" mark as passed comment as hello", testInstance.extract_markAs_status_as_first);
        Assert.assertEquals(result.size(),0);
    }

    @Test
    public void shouldMatchGoBack(){
        over(
                "Should exact pass",
                strs("go back", " go back ", "go back  "),
                checkExactPass(testInstance.command_goBack)
        );
    }

    @Test
    public void shouldNotMatchGoBack(){
        over(
                "Should not exact pass",
                strs("something go back", "go bac ", "go back something  after"),
                checkNotExactPass(testInstance.command_goBack)
        );
    }


    @Test
    public void shouldCatchMarkAs(){
        over(
                "Should accept",
                strs("m", "Ma", "mArk", "maRk ", "mark a"),
                checkPass(testInstance.command_markAs)
        );
    }

    @Test
    public void shouldCatchNotMarkAs(){
        over(
                "Should not accept",
                strs("mark as ","mm","mask as p","mark as pass"),
                checkNotPass(testInstance.command_markAs)
        );
    }

    @Test
    public void shouldCatchMarkAsWithStatus(){
        over(
                "Should accept =mark with passed=",
                strs("mark as ","mark as p","mark as pass","mark as passe"),
                checkPass(testInstance.command_markAs_passed)
        );
    }

    @Test
    public void shouldCatchNotMarkAsWithStatus(){
        over(
                "Should not accept for =mark as passed=",
                strs("mask as ","mark as passed","mark as paasd"),
                checkNotPass(testInstance.command_markAs_passed)
        );
    }



    @Test
    public void shouldCatchMarkAndComment(){
        over(
                "Should accept =mark as passed and comment=",
                strs("mark as passed ",
                     "mark as passed an",
                     "mark as passed and com",
                     "mark as passed and commen"
                ),
                checkPass(testInstance.command_markAs_passed_andComment)
        );
    }

    @Test
    public void shouldCatchNotMarkAndComment(){
        over(
                "Should not accept =mark as passed and comment=",
                strs(
                        "mark as passed",
                        "mark as passed and comment",
                        "mark as passed and comment mark as passed and com"
                ),
                checkNotPass(testInstance.command_markAs_passed_andComment)
        );
    }

    public String[] strs(String... strings){
        return strings;
    }

    public void over(String testName, String[] strings, Closure<Boolean,String> operationClosure){
        for (String string : strings) {
            if (!operationClosure.call(string)){
                throw new AssertionError("'"+testName + "' failed against string: "+string);
            }
        }

    }


    private Closure<Boolean, String> checkExactPass(final TextRecognitionCore.TextCommand pattern) {
        return new Closure<Boolean, String>() {
            @Override
            public Boolean call(String s) {
                return testInstance.checkExactAgainst(s, pattern);
            }
        };
    }

    private Closure<Boolean, String> checkNotExactPass(final TextRecognitionCore.TextCommand pattern) {
        return new Closure<Boolean, String>() {
            @Override
            public Boolean call(String s) {
                return !testInstance.checkExactAgainst(s, pattern);
            }
        };
    }

    private Closure<Boolean, String> checkPass(final TextRecognitionCore.TextCommand pattern) {
        return new Closure<Boolean, String>() {
            @Override
            public Boolean call(String s) {
                return testInstance.checkAgainst(s,pattern);
            }
        };
    }
    private Closure<Boolean, String> checkNotPass(final TextRecognitionCore.TextCommand pattern) {
        return new Closure<Boolean, String>() {
            @Override
            public Boolean call(String s) {
                return !testInstance.checkAgainst(s,pattern);
            }
        };
    }
}