package intellispaces.ixora.rdb;

import intellispaces.framework.core.annotation.Guide;
import intellispaces.ixora.structures.collection.JavaList;

import java.util.ArrayList;
import java.util.List;

@Guide
public class DefaultNamedQueryToBlindQueryGuide implements NamedQueryToBlindQueryGuide {

  @Override
  public BlindQueryAndParameterNames namedQueryToBlindQuery(String query) {
    char[] originQuery = query.toCharArray();
    char[] blindQuery = new char[query.length()];
    List<String> paramNames = new ArrayList<>();
    int index1 = 0, index2 = 0;
    while (index1 < originQuery.length) {
      char ch = originQuery[index1++];
      if (ch == ':') {
        blindQuery[index2++] = '?';
        int ind = index1;
        while (index1 < originQuery.length && Character.isLetterOrDigit(originQuery[index1])) {
          index1++;
        }
        String paramName = query.substring(ind, index1);
        paramNames.add(paramName);
      } else {
        blindQuery[index2++] = ch;
      }
    }
    return new BlindQueryAndParameterNamesData(
      new String(blindQuery, 0, index2),
      new JavaList<>(paramNames, String.class)
    );
  }
}
