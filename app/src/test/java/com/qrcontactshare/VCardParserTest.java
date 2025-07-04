package com.qrcontactshare;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class VCardParserTest {

    private static final String vcard =
    "BEGIN:VCARD\n" +
    "VERSION:2.1\n" +
    "N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=50=C3=A9=72=65=7A=20=4D=61=72=74=C3=AD=6E=65=7A=20;=41=6C=69=65=64;;;\n" +
    "FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=41=6C=69=65=64=20=50=C3=A9=72=65=7A=20=4D=61=72=74=C3=AD=6E=65=7A\n" +
    "TEL;CELL;PREF:+1-555-123-4567\n" +
    "EMAIL;PREF;HOME:thejavadruid@gmail.com\n" +
    "PHOTO;ENCODING=BASE64;JPEG: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\n" +
    "\n" +
    "END:VCARD\n";

    @Test
    public void testGetText() throws IOException {
        final String expected =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=50=C3=A9=72=65=7A=20=4D=61=72=74=C3=AD=6E=65=7A=20;=41=6C=69=65=64;;;\n" +
            "FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=41=6C=69=65=64=20=50=C3=A9=72=65=7A=20=4D=61=72=74=C3=AD=6E=65=7A\n" +
            "TEL;CELL;PREF:+1-555-123-4567\n" +
            "EMAIL;PREF;HOME:thejavadruid@gmail.com\n" +
            "\n" +
            "END:VCARD\n";

        final VCardParser instance = new VCardParser(new ByteArrayInputStream(vcard.getBytes()));

        assertEquals(expected, instance.getText());
    }
}