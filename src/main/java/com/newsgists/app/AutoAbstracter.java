package com.newsgists.app;

/**
 * Created by abhinav on 11/7/16.
 */
public interface AutoAbstracter {

    /**
     * Performs an automated abstract generation for the given text.
     *
     * @param text
     * @return the abstract
     */
    String summarize(String text);

}
