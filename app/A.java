<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/splash_gradient"
    android:padding="16dp">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/historyToolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/transparent"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar" />

    <TextView
        android:id="@+id/historyTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Tasks for Selected Date"
        android:textColor="@android:color/white"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginVertical="8dp" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/historyRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
</LinearLayout>
