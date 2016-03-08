package Geoserver;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;

public class GeoserverStyleSet {

	private static String getBodySld(String styleType, final String symbol,
			final int durchmesser, final String fuellfarbe,
			final String strichfarbe, final int strichstaerke) {
		String bodySld = "";
		if (styleType.contains("Point")) {
			bodySld = getPointSld(symbol, durchmesser, fuellfarbe);
		} else if (styleType.contains("Line")) {
			bodySld = getLineSld(strichfarbe, strichstaerke);

		} else if (styleType.contains("Polygon")
				|| styleType.contains("MultiPolygon")) {
			bodySld = getPolygonSld(fuellfarbe, strichfarbe, strichstaerke);
		}

		return bodySld;
	}

	private static String getFilterBereicheSld(final String attr,
			final String wertGreaterEquals, final String wertLess) {
		// @formatter:off
		final String filterSld = "" + "          <ogc:Filter>\n"
				+ "            <ogc:And>\n"
				+ "              <ogc:PropertyIsGreaterThanOrEqualTo>\n"
				+ "                <ogc:PropertyName>%s</ogc:PropertyName>\n"
				+ "                <ogc:Literal>%s</ogc:Literal>\n"
				+ "              </ogc:PropertyIsGreaterThanOrEqualTo>\n"
				+ "              <ogc:PropertyIsLessThan>\n"
				+ "                <ogc:PropertyName>%s</ogc:PropertyName>\n"
				+ "                <ogc:Literal>%s</ogc:Literal>\n"
				+ "              </ogc:PropertyIsLessThan>\n"
				+ "            </ogc:And>\n" + "          </ogc:Filter>\n";
		// @formatter:on
		final String attrEsc = StringEscapeUtils.escapeXml(attr);
		final String wertGreaterEqualsEsc = StringEscapeUtils
				.escapeXml(wertGreaterEquals);
		final String wertLessEsc = StringEscapeUtils.escapeXml(wertLess);

		final String filter = String.format(Locale.US, filterSld, attrEsc,
				wertGreaterEqualsEsc, attrEsc, wertLessEsc);
		return filter;
	}

	private static String getFilterEinzelwertSld(final String attr,
			final String wertEquals) {
		// @formatter:off
		final String filterSld = "" + "          <ogc:Filter>\n"
				+ "            <ogc:PropertyIsEqualTo>\n"
				+ "              <ogc:PropertyName>%s</ogc:PropertyName>\n"
				+ "              <ogc:Literal>%s</ogc:Literal>\n"
				+ "            </ogc:PropertyIsEqualTo>\n"
				+ "          </ogc:Filter>";
		// @formatter:on
		final String attrEsc = StringEscapeUtils.escapeXml(attr);
		final String wertEqualsEsc = StringEscapeUtils.escapeXml(wertEquals);
		final String filter = String.format(Locale.US, filterSld, attrEsc,
				wertEqualsEsc);
		return filter;
	}

	private static String getFooterSld() {
		// @formatter:off
		final String footerSld = "" + "      </FeatureTypeStyle>\n"
				+ "    </UserStyle>\n" + "  </NamedLayer>\n"
				+ "</StyledLayerDescriptor>\n";
		// @formatter:on
		return footerSld;
	}

	private static String getHeaderSld(final String styleName) {
		// @formatter:off
		final String headerSld = ""
				+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ ""
				+ "<StyledLayerDescriptor version=\"1.0.0\" \n"
				+ " xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" \n"
				+ " xmlns=\"http://www.opengis.net/sld\" \n"
				+ " xmlns:ogc=\"http://www.opengis.net/ogc\" \n"
				+ " xmlns:xlink=\"http://www.w3.org/1999/xlink\" \n"
				+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
				+ "  <!-- a Named Layer is the basic building block of an SLD document -->\n"
				+ "  <NamedLayer>\n"
				+ "    <Name>%s</Name>\n"
				+ "    <UserStyle>\n"
				+ "    <!-- Styles can have names, titles and abstracts -->\n"
				+ "      <Name>%s</Name>\n"
				+ "      <Title>%s</Title>\n"
				+ "      <Abstract>A sample style based on user defined input for a layer with the very same name</Abstract>\n"
				+ "      <!-- FeatureTypeStyles describe how to render different features -->\n"
				+ "      <!-- A FeatureTypeStyle for rendering points -->\n"
				+ "      <FeatureTypeStyle>\n";
		// @formatter:on
		final String styleNameEsc = StringEscapeUtils.escapeXml(styleName);
		final String header = String.format(Locale.US, headerSld, styleNameEsc,
				styleNameEsc, styleNameEsc);
		return header;
	}

	private static String getLineSld(final String strichfarbe,
			final int strichstaerke) {
		final boolean noStrichfarbe = "none".equals(strichfarbe);
		String sld = "";
		// @formatter:off
		final String lineSld = ""
				+ "			<!-- like a polygonsymbolizer -->\n"
				+ "			<LineSymbolizer>\n"
				+ (noStrichfarbe ? ""
						: "				<Stroke>\n"
								+ "					<CssParameter name=\"stroke\">%s</CssParameter>\n"
								+ "					<CssParameter name=\"stroke-width\">%s</CssParameter>\n"
								+ "				</Stroke>\n") + "			</LineSymbolizer>\n";
		// @formatter:on
		if (noStrichfarbe) {
			sld = String.format(Locale.US, lineSld);
		} else {
			sld = String.format(Locale.US, lineSld, strichfarbe, strichstaerke);
		}

		return sld;

	}

	private static String getPointSld(final String symbol,
			final int durchmesser, final String fuellfarbe) {
		final boolean noFuellfarbe = "none".equals(fuellfarbe);
		String sld = "";
		// @formatter:off
		final String pointSld = ""
				+ "            <PointSymbolizer>\n"
				+ "              <Graphic>\n"
				+ "                <Mark>\n"
				+ "                  <WellKnownName>%s</WellKnownName>\n"
				+ (noFuellfarbe ? ""
						: "                  <Fill>\n"
								+ "                    <CssParameter name=\"fill\">%s</CssParameter>\n"
								+ "                  </Fill>\n")
				+ "                </Mark>\n"
				+ "              <Size>%s</Size>\n"
				+ "            </Graphic>\n" + "          </PointSymbolizer>\n";
		// @formatter:on
		if (noFuellfarbe) {
			sld = String.format(Locale.US, pointSld, symbol, durchmesser);
		} else {
			sld = String.format(Locale.US, pointSld, symbol, fuellfarbe,
					durchmesser);
		}
		return sld;

	}

	private static String getPolygonSld(final String fuellfarbe,
			final String strichfarbe, final int strichstaerke) {
		final boolean noFuellfarbe = "none".equals(fuellfarbe);
		final boolean noStrichfarbe = "none".equals(strichfarbe);
		String sld = "";

		// @formatter:off
		final String polygonSld = ""
				+ "          <!-- like a linesymbolizer but with a fill too -->\n"
				+ "          <PolygonSymbolizer>\n"
				+ (noFuellfarbe ? ""
						: "            <Fill>\n"
								+ "              <CssParameter name=\"fill\">%s</CssParameter>\n"
								+ "            </Fill>\n")
				+ (noStrichfarbe ? ""
						: "            <Stroke>\n"
								+ "              <CssParameter name=\"stroke\">%s</CssParameter>\n"
								+ "              <CssParameter name=\"stroke-width\">%s</CssParameter>\n")
				+ "            </Stroke>\n" + "          </PolygonSymbolizer>";
		// @formatter:on
		if (noFuellfarbe) {
			if (noStrichfarbe) {
				sld = String.format(Locale.US, polygonSld);
			} else {
				sld = String.format(Locale.US, polygonSld, strichfarbe,
						strichstaerke);
			}
		} else {
			if (noStrichfarbe) {
				sld = String.format(Locale.US, polygonSld, fuellfarbe);
			} else {
				sld = String.format(Locale.US, polygonSld, fuellfarbe,
						strichfarbe, strichstaerke);
			}
		}
		return sld;
	}

	private static String getRuleFooterSld() {
		// @formatter:off
		final String ruleFooterSld = "" + "        </Rule>\n";
		// @formatter:on
		final String ruleFooter = String.format(Locale.US, ruleFooterSld);
		return ruleFooter;
	}

	private static String getRuleHeaderSld(final String beschriftung) {
		// @formatter:off
		final String ruleHeaderSld = "" + "        <Rule>\n"
				+ "          <Name>%s</Name>\n"
				+ "          <Title>%s</Title>\n"
				+ "          <Abstract>Adds a geometry</Abstract>\n";
		// @formatter:on
		final String beschriftungEsc = StringEscapeUtils
				.escapeXml(beschriftung);
		final String ruleHeader = String.format(Locale.US, ruleHeaderSld,
				beschriftungEsc, beschriftungEsc);
		return ruleHeader;
	}

	/**
	 * Create a more complex SLD
	 *
	 * @param bnbMapLayer
	 *            the layer obj
	 *
	 * @param selectedAttr
	 *            das shape Attribute mit den Klassen
	 */
	public static String doComplexSLD(String name, String styleType, String symbol,
			String attr, List<Object> attrClasses) {
		final String styleName = name;
		// Oracle stores attribute names in upper case
		final String attrName = attr.toUpperCase();
		final String header = getHeaderSld(styleName);
		final String footer = getFooterSld();

		final StringBuilder sld = new StringBuilder();

		sld.append(header);

		// need to consider all added plus the tmp class

		for (int i = 1; i < attrClasses.size(); i++) {
			final String ruleHeader;
			final String filter;
			if (attrClasses.get(0).equals("Einzelwerte")) {
				 ruleHeader = getRuleHeaderSld(attrClasses.get(i)
					.toString());
				filter = getFilterEinzelwertSld(attrName, attrClasses.get(i)
						.toString());
			} else {
				
				if (i + 1 < attrClasses.size()) {
					ruleHeader = getRuleHeaderSld(attrClasses.get(i).toString()+" to "+attrClasses.get(i+1).toString());
					filter = getFilterBereicheSld(attrName, attrClasses.get(i)
							.toString(), attrClasses.get(i + 1).toString());
				} else {
					ruleHeader = getRuleHeaderSld("Greater than "+attrClasses.get(i).toString());
					filter = getFilterBereicheSld(attrName, attrClasses.get(i)
							.toString(), "2147483646");
				}
			}

			// TODO: für Body symbolgröße anpassbar, usw
			final String body = getBodySld(styleType, symbol, 8, rdColor(),
					rdColor(), 1);
			final String ruleFooter = getRuleFooterSld();
			if (!filter.equals("")) {
				sld.append(ruleHeader + filter + body + ruleFooter);
			}
		}

		sld.append(footer);
		return (sld.toString());

	}

	private static String rdColor() {
		Random rd = new Random();
		int number = (int) (16777215 * rd.nextFloat());
		return "#" + Integer.toHexString(number);
	}

	/**
	 * Creates a non-classified SLD
	 *
	 * @param bnbMapLayer
	 *            the layer obj
	 *
	 * @param beschriftung
	 *            Legendenbeschriftung
	 * @param symbol
	 *            circle, square, star
	 * @param strichstaerke
	 *            in Pixel
	 * @param durchmesser
	 *            in Pixel
	 * @param strichfarbe
	 *            Farbe der Umrandung
	 * @param fuellfarbe
	 *            Farbe der Füllung
	 */

	public static String doSimpleSLD(String styleName, String styleType,
			final String beschriftung, final String symbol,
			final int durchmesser, final int strichstaerke) {
		final String name = styleName;
		final String header = getHeaderSld(name);
		final String ruleHeader = getRuleHeaderSld(beschriftung);
		final String ruleFooter = getRuleFooterSld();
		final String footer = getFooterSld();
		final String body = getBodySld(styleType, symbol, durchmesser,
				rdColor(), rdColor(), strichstaerke);

		final String sld = header + ruleHeader + body + ruleFooter + footer;
		return (sld);
	}

}
