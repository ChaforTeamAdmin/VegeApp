package com.jby.vegeapp.shareObject;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class CustomScheduleJob {
    public static void scheduleJob(final Context context, final ComponentName componentName, final int jobId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JobInfo info = new JobInfo.Builder(jobId, componentName)
                        .setRequiresCharging(false)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPersisted(true)
                        .build();

                JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

                int resultCode = 0;
                if (scheduler != null) {
                    resultCode = scheduler.schedule(info);
                }
                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d("CustomScheduleJob", "Job scheduled");
                } else {
                    Log.d("CustomScheduleJob", "Job scheduling failed");
                }
            }
        }).start();
    }
}
