/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.messages.PlatformMessages;

/**
 * Converts between radix 10,2,8 & 16
 */
public class RadixConverter {

	/**
	 * 
	 */
	public static float parseFloat(String value) {
		if (value.matches("-?[0-9]*(\\.[0-9]*)?(E([+\\-]?)[0-9]*)?")) { //$NON-NLS-1$
			return Float.valueOf(value);
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return Float.valueOf(binToDec(value));
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return Float.valueOf(octToDec(value));
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return Float.valueOf(hexToDec(value));
		} else {
			throw new NumberFormatException(NLS.bind(PlatformMessages.error_cantBeParsedToFloat, value));
		}
	}

	/**
	 *
	 */
	public static int parseInt(String value) {

		if (value.matches("-?[0-9]*")) { //$NON-NLS-1$
			return Integer.parseInt(value);
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return binToDec(value);
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return octToDec(value);
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return hexToDec(value);
		} else {
			throw new NumberFormatException(NLS.bind(PlatformMessages.error_cantBeParsedToInt, value));
		}
	}

	/**
	 * 
	 */
	public static int getRadix(String value) {
		if (value.matches("-?[0-9]*(\\.[0-9]*)?(E([+\\-]?)[0-9]*)?")) { //$NON-NLS-1$
			return 10;
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return 2;
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return 8;
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return 16;
		} else {
			return 0;
		}
	}

	public static String convert(String value, int radix) {
		switch (radix) {
		case 2:
			return convertToBin(value);
		case 8:
			return convertToOct(value);
		case 10:
			return convertToDec(value);
		case 16:
			return convertToHex(value);
		default:
			return value;
		}
	}

	public static String convertToBin(String value) {
		if (value.matches("-?[0-9]*")) { //$NON-NLS-1$
			return decToBin(Integer.parseInt(value));
		} else if (value.matches("-?[0-9]*(\\.[0-9]*)?(E([+\\-]?)[0-9]*)?")) { //$NON-NLS-1$
			return decToBin(Float.valueOf(value).intValue());
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return decToBin(octToDec(value));
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return decToBin(hexToDec(value));
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return value;
		} else {
			throw new NumberFormatException(NLS.bind(PlatformMessages.error_cantBeConvertedToBinary, value));
		}
	}

	public static String convertToOct(String value) {
		if (value.matches("-?[0-9]*")) { //$NON-NLS-1$
			return decToOct(Integer.parseInt(value));
		} else if (value.matches("-?[0-9]*(\\.[0-9]*)?(E([+\\-]?)[0-9]*)?")) { //$NON-NLS-1$
			return decToOct(Float.valueOf(value).intValue());
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return binToOct(value);
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return hexToOct(value);
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return value;
		} else {
			throw new NumberFormatException(NLS.bind(PlatformMessages.error_cantBeConvertedToOctal, value));
		}
	}

	public static String convertToDec(String value) {
		if (value.matches("-?[0-9]*(\\.[0-9]*)?(E([+\\-]?)[0-9]*)?")) { //$NON-NLS-1$
			return Float.valueOf(value).toString();
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return Integer.toString(octToDec(value));
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return Integer.toString(hexToDec(value));
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return String.valueOf(binToDec(value));
		} else {
			throw new NumberFormatException(NLS.bind(PlatformMessages.error_cantBeConvertedToDecimal, value));
		}
	}

	public static String convertToHex(String value) {
		if (value.matches("-?[0-9]*")) { //$NON-NLS-1$
			return decToHex(Integer.parseInt(value));
		} else if (value.matches("-?[0-9]*(\\.[0-9]*)?(E([+\\-]?)[0-9]*)?")) { //$NON-NLS-1$
			return decToHex(Float.valueOf(value).intValue());
		} else if (value.matches("0[1-7][0-7]*")) { //$NON-NLS-1$
			return octToHex(value);
		} else if (value.matches("0b[0-1]*")) { //$NON-NLS-1$
			return binToHex(value);
		} else if (value.matches("0x[0-9a-zA-Z]*")) { //$NON-NLS-1$
			return value;
		} else {
			throw new NumberFormatException(NLS.bind(PlatformMessages.error_cantBeConvertedToHex, value));
		}
	}

	/**
	 * Converts from binary to decimal
	 * 
	 * @param bin
	 *            the string to convert
	 * @return the decimal value
	 */
	public static int binToDec(String bin) {
		if (bin.startsWith("0b")) { //$NON-NLS-1$
			bin = bin.substring(2);
		}
		int nb = Integer.parseInt(bin, 2);
		return nb;
	}

	/**
	 * Converts from binary to octal
	 * 
	 * @param bin
	 * @return the octal value of bin as string
	 */
	public static String binToOct(String bin) {
		int nb = binToDec(bin);
		return decToOct(nb);
	}

	/**
	 * Converts from binary to hex
	 * 
	 * @param bin
	 * @return the hex value of bin
	 */
	public static String binToHex(String bin) {
		int nb = binToDec(bin);
		return Integer.toHexString(nb).toUpperCase();
	}

	/**
	 * Converts from octal to binary
	 * 
	 * @param oct
	 * @return the binary value of oct
	 */
	public static String octToBin(String oct) {
		if (oct.startsWith("0")) { //$NON-NLS-1$
			oct = oct.substring(1);
		}
		int nb = Integer.parseInt(oct, 8);
		return decToBin(nb);
	}

	/**
	 * Converts from octal to decimal
	 * 
	 * @param oct
	 *            the octal value as string
	 * @return the decimal value of oct
	 */
	public static int octToDec(String oct) {
		if (oct.startsWith("0")) { //$NON-NLS-1$
			oct = oct.substring(1);
		}
		return Integer.parseInt(oct, 8);
	}

	/**
	 * Converts from octal to hex
	 * 
	 * @param oct
	 * @return the hex value of oct
	 */
	public static String octToHex(String oct) {
		int nb = octToDec(oct);
		return decToHex(nb).toUpperCase();
	}

	/**
	 * Converts from decimal to binary
	 * 
	 * @param nb
	 *            the decimal value
	 * @return the binary value as string
	 */
	public static String decToBin(int nb) {
		String str = Integer.toBinaryString(nb);

		return "0b".concat(str); //$NON-NLS-1$
	}

	/**
	 * Converts from decimal to octal
	 * 
	 * @param dec
	 * @return the octal value of dec
	 */
	public static String decToOct(int dec) {
		return "0".concat(Integer.toOctalString(dec)); //$NON-NLS-1$
	}

	/**
	 * Converts from decimal to hex
	 * 
	 * @param dec
	 * @return the hex value of dec
	 */
	public static String decToHex(int dec) {
		return "0x".concat(Integer.toHexString(dec).toUpperCase()); //$NON-NLS-1$
	}

	/**
	 * Converts from Hex to binary
	 * 
	 * @param hex
	 *            the hex value as string
	 * @return the binary value of hex
	 */
	public static String hexToBin(String hex) {
		if (hex.startsWith("0x")) { //$NON-NLS-1$
			hex = hex.substring(2);
		}
		int nb = Integer.parseInt(hex, 16);
		return decToBin(nb);
	}

	/**
	 * Converts from hex to octal
	 * 
	 * @param hex
	 * @return the octal value of hex
	 */
	public static String hexToOct(String hex) {
		int nb = hexToDec(hex);
		return decToOct(nb);
	}

	/**
	 * Converts from Hex to decimal
	 * 
	 * @param hex
	 *            hex numerical value as string
	 * @return the decimal value of hex
	 */
	public static int hexToDec(String hex) {
		if (hex.startsWith("0x")) { //$NON-NLS-1$
			hex = hex.substring(2);
		}
		int ch = Integer.parseInt(hex, 16);
		return ch;
	}
}
