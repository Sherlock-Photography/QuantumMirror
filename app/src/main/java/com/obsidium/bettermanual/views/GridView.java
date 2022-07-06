package com.obsidium.bettermanual.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.sony.scalar.hardware.avio.DisplayManager;

public class GridView extends View
{
    private final Paint                 m_paint = new Paint();
    private DisplayManager.VideoRect    m_videoRect;

    public GridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        m_paint.setAntiAlias(false);
        m_paint.setARGB(100, 100, 100, 100);
        m_paint.setStrokeWidth(2);
        m_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    public void setVideoRect(DisplayManager.VideoRect videoRect)
    {
        m_videoRect = videoRect;
    }

    public void onDraw(Canvas canvas)
    {
        canvas.drawARGB(0, 0, 0, 0);

        if (m_videoRect != null)
        {
            final float h = getHeight();

            final float middleX = (float) (m_videoRect.pxRight + m_videoRect.pxLeft) / 2;
            final float squareSize = m_videoRect.pxBottom - m_videoRect.pxTop;

            // Vertical lines
            canvas.drawLine(middleX - squareSize / 2, 0, middleX - squareSize / 2, h, m_paint);
            canvas.drawLine(middleX + squareSize / 2, 0, middleX + squareSize / 2, h, m_paint);
        }
    }
}
