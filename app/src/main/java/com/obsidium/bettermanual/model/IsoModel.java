package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

import java.util.List;

public class IsoModel extends AbstractModel<String> implements CameraEx.AutoISOSensitivityListener {


    private int m_curIso;
    private int currentIsoPos;
    private List<Integer> m_supportedIsos;

    public IsoModel(CameraInstance cameraInstance)
    {
        super(cameraInstance);
        this.m_supportedIsos = camera.getSupportedISOSensitivities();
        m_curIso = camera.getISOSensitivity();
        value = "\uE488 " + String.valueOf(0) + (m_curIso == 0 ? "(A)" : "");
        currentIsoPos = getIsoPos(m_curIso);
        fireOnValueChanged();
        fireOnSupportedChanged();

    }

    private int getIsoPos(int isovalue)
    {
        for (int i= 0; i< m_supportedIsos.size();i++)
        {
            if (m_supportedIsos.get(i) == isovalue)
                return i;
        }
        return 0;
    }

    @Override
    public void setValue(int i) {
        currentIsoPos += i;

        if (currentIsoPos < 0)
            currentIsoPos = 0;
        if (currentIsoPos >= m_supportedIsos.size())
            currentIsoPos = m_supportedIsos.size()-1;

        m_curIso = m_supportedIsos.get(currentIsoPos);
        value = String.format("\uE488 %s", (m_curIso == 0 ? "AUTO" : String.valueOf(m_curIso)));

        camera.setISOSensitivity(m_curIso);
        fireOnValueChanged();
    }

    public void toggle()
    {
        m_curIso = getCurrentIso() == 0 ? getFirstManualIso() : 0;
        value = String.format("\uE488 %s", (m_curIso == 0 ? "AUTO" : String.valueOf(m_curIso)));

        camera.setISOSensitivity(m_curIso);
        fireOnValueChanged();
    }

    @Override
    public String getValue() {
        return value;
    }


    @Override
    public boolean isSupported() {
        return true;
    }

    //AutoIsoSensitivityListener
    @Override
    public void onChanged(int i, CameraEx cameraEx) {
        value = "\uE488 " + String.valueOf(i) + (m_curIso == 0 ? "(A)" : "");
        fireOnValueChanged();
    }


    public int getCurrentIso()
    {
        return m_curIso;
    }

    public int getFirstManualIso() {
        if (m_supportedIsos == null)
            return 0;

        return m_supportedIsos.get(1);
    }
}
