package com.philleeran.flicktoucher.view.pad.adapter;

import com.philleeran.flicktoucher.view.pad.PadPresenter;

/**
 * Created by young on 2017-03-10.
 */

public interface PadGridViewAdapterContract
{
    interface View{

        void setBindListener(BindListener padPresenter);
    }

    interface Model
    {

    }
}
