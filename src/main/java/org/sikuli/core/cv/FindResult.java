/*******************************************************************************
 * Copyright 2011 sikuli.org
 * Released under the MIT license.
 * 
 * Contributors:
 *     Tom Yeh - initial API and implementation
 ******************************************************************************/
package org.sikuli.core.cv;

import java.awt.Rectangle;

public class FindResult extends Rectangle implements Comparable<FindResult> {   
   public double score;  
   
   public String toString(){
      return "x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", score=" + score;
   }

   @Override
   public int compareTo(FindResult o) {
      if (score - o.score < 0){
         return -1;
      }else if (score - o.score > 0){
         return 1;
      }else{
         return 0;
      }
   }
}
