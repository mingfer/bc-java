package org.bouncycastle.crypto.digests;

import org.bouncycastle.util.Arrays;

public class CSHAKEDigest
    extends SHAKEDigest
{
    private static final byte[] padding = new byte[100];
    private final byte[] diff;

    public CSHAKEDigest(int bitLength, byte[] N, byte[] S)
    {
        super(bitLength);

        if ((N == null || N.length == 0) && (S == null || S.length == 0))
        {
            diff = null;
        }
        else
        {
            diff = Arrays.concatenate(leftEncode(rate / 8), encodeString(N), encodeString(S));
            diffPadAndAbsorb();
        }
    }

    private void diffPadAndAbsorb()
    {
        int blockSize = rate / 8;
        absorb(diff, 0, diff.length);

        int required = blockSize - (diff.length % blockSize);

        while (required > padding.length)
        {
            absorb(padding, 0, padding.length);
            required -= padding.length;
        }
        
        absorb(padding, 0, required);
    }

    private byte[] encodeString(byte[] str)
    {
        if (str == null || str.length == 0)
        {
            return leftEncode(0);
        }

        return Arrays.concatenate(leftEncode(str.length * 8), str);
    }

    private byte[] leftEncode(int strLen)
    {
    	byte n = 0;

    	for (int v = strLen; v != 0; v = v >> 8)
        {
    		n++;
    	}

        if (n == 0)
        {
    		n = 1;
    	}

        byte[] b = new byte[n + 1];

    	b[0] = n;
    	for (int i = 1; i <= n; i++)
    	{
    		b[i] = (byte)(strLen >> (8 * (i - 1)));
    	}

    	return b;
    }
    
    public int doOutput(byte[] out, int outOff, int outLen)
    {
        if (diff != null)
        {
            if (!squeezing)
            {
                absorbBits(0x00, 2);
            }

            squeeze(out, outOff, ((long)outLen) * 8);

            return outLen;
        }
        else
        {
            return super.doOutput(out, outOff, outLen);
        }
    }

    public void reset()
    {
        super.reset();
        
        if (diff != null)
        {
            diffPadAndAbsorb();
        }
    }
}