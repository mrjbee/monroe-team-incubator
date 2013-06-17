package org.monroe.team.jfeature.utils;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 11:20 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Pair<FirstType,SecondType> {

    public final FirstType first;
    public final SecondType second;

    public Pair(FirstType first, SecondType second) {
        this.first = first;
        this.second = second;
    }
}
