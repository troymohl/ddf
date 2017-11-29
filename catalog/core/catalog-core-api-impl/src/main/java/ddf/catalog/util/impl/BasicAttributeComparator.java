/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package ddf.catalog.util.impl;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Result;
import java.io.Serializable;
import java.util.Comparator;
import org.apache.commons.collections.ComparatorUtils;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Comparator for the relevance of 2 {@link Result} objects. */
public class BasicAttributeComparator implements Comparator<Result> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicAttributeComparator.class);

  private SortOrder sortOrder;

  private String attributeName;

  /**
   * Constructs the comparator with the specified sort order, either relevance ascending or
   * relevance descending.
   *
   * @param relevanceOrder the relevance sort order
   */
  public BasicAttributeComparator(String attributeName, SortOrder relevanceOrder) {
    this.attributeName = attributeName;
    this.sortOrder = relevanceOrder;
  }

  /**
   * Compares the relevance between the two results.
   *
   * @return 1 if A is null and B is non-null -1 if A is non-null and B is null 0 if both A and B
   *     are null 1 if ascending relevance and A > B; -1 if ascending relevance and B > A -1 if
   *     descending relevance and A > B; 1 if descending relevance and B > A
   */
  @Override
  public int compare(Result contentA, Result contentB) {
    if (contentA != null && contentB != null) {
      Attribute contentAttrA = contentA.getMetacard().getAttribute(attributeName);
      Attribute contentAttrB = contentB.getMetacard().getAttribute(attributeName);
      if (contentAttrA == null && contentAttrB != null) {
        return -1;
      } else if (contentAttrA != null && contentAttrB == null) {
        return 1;
      } else {
        Serializable attrValueA = contentAttrA.getValue();
        Serializable attrValueB = contentAttrB.getValue();

        if (attrValueA == null && attrValueB != null) {
          return -1;
        } else if (attrValueA != null && attrValueB == null) {
          return 1;
        } else {
          int compareValue = ComparatorUtils.NATURAL_COMPARATOR.compare(attrValueA, attrValueB);
          return sortOrder == SortOrder.ASCENDING ? compareValue : -1 * compareValue;
        }
      }
    } else if (contentA != null && contentB == null) {
      return 1;
    } else {
      return -1;
    }
  }
}
