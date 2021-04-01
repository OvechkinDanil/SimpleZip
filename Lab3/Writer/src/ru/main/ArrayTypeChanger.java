package ru.main;
import ru.spbstu.pipeline.TYPE;

import java.nio.ByteBuffer;

public class ArrayTypeChanger {

    public byte[] byteArray(Object oArr, TYPE uType)
    {
        switch (uType)
        {
            case BYTE:
                return (byte[])oArr;
            case CHAR:
                return charArrayToBytesArray(oArr);
            case SHORT:
                return shortArrayToByteArray(oArr);
            default:
                return null;
        }
    }


    private byte[] charArrayToBytesArray(Object oArr)
    {
        char[] cArr = (char[])oArr;
        if (cArr == null || cArr.length % 2 != 0)
            return null;

        short[] sArr = new short[cArr.length];

        for (int i = 0; i < sArr.length; i++)
        {
            sArr[i] = (short)cArr[i];
        }
        return shortArrayToByteArray(sArr);
    }

    public short[] byteArrayToShortArray(byte[] mergedArray)
    {
        if (mergedArray == null || mergedArray.length % 2 != 0)
            return null;

        int newBufSize = mergedArray.length;

        short[] shortBuffer = new short[newBufSize / 2];

        ByteBuffer byteBuffer = ByteBuffer.wrap(mergedArray);

        for (int i = 0; i < shortBuffer.length; i++)
        {
            shortBuffer[i] = byteBuffer.getShort(2*i);
        }

        if (shortBuffer.length == 0)
            return null;

        return shortBuffer;
    }

    private byte[] shortArrayToByteArray(Object oArr)
    {
        short[] sArr = (short[])oArr;

        if (sArr == null || sArr.length % 2 != 0)
            return null;

        ByteBuffer b = ByteBuffer.allocate(2 * sArr.length);

        for (short value : sArr) {
            b.putShort(value);
        }

        return b.array();
    }


}
