package com.laxture.lib.connectivity.http;

public interface IProgressMonitor {

    public void setProgress(int max, int progress);
    public void setText(String text);

}
