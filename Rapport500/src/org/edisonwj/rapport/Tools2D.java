package org.edisonwj.rapport;

/**
 * Tools2D provides miscellaneous geometric processing tools.
 * Uses: Pointd and Triangle
 * 
 * Copied from Section 2.13 of:
 *    Ammeraal, L. (1998) Computer Graphics for Java Programmers,
 *       Chichester: John Wiley.
 */

class Tools2D
{  static double area2(Pointd A, Pointd B, Pointd C)
   {  return (A.x - C.x) * (B.y - C.y) - (A.y - C.y) * (B.x - C.x);
   }

   static boolean insideTriangle(Pointd A, Pointd B, Pointd C, 
      Pointd P) // ABC is assumed to be counter-clockwise
   {  return
        Tools2D.area2(A, B, P) >= 0 && 
        Tools2D.area2(B, C, P) >= 0 && 
        Tools2D.area2(C, A, P) >= 0;
   }

   static void triangulate(Pointd[] P, Triangle[] tr)
   {  // P contains all n polygon vertices in CCW order.
      // The resulting triangles will be stored in array tr.
      // This array tr must have length n - 2.
      int n = P.length, j = n - 1, iA=0, iB, iC;
      int[] next = new int[n];
      for (int i=0; i<n; i++) 
      {  next[j] = i;
         j = i;
      }
      for (int k=0; k<n-2; k++)
      {  // Find a suitable triangle, consisting of two edges
         // and an internal diagonal:
         Pointd A, B, C;
         boolean triaFound = false;
         int count = 0;
         while (!triaFound && ++count < n)
         {  iB = next[iA]; iC = next[iB];
            A = P[iA]; B = P[iB]; C = P[iC];
            if (Tools2D.area2(A, B, C) >= 0)
            {  // Edges AB and BC; diagonal AC.
               // Test to see if no other polygon vertex
               // lies within triangle ABC:
               j = next[iC];
               while (j != iA && !insideTriangle(A, B, C, P[j]))
                  j = next[j];
               if (j == iA) 
               {  // Triangle ABC contains no other vertex:
                  tr[k] = new Triangle(A, B, C);
                  next[iA] = iC; 
                  triaFound = true;
               }  
            }  
            iA = next[iA];
         }
         if (count == n)
         {  System.out.println("Not a simple polygon" +
              " or vertex sequence not counter-clockwise.");
            System.exit(1);
         }
      }
   }

   static double distance2(Pointd P, Pointd Q)
   {  double dx = P.x - Q.x, dy = P.y - Q.y;
      return dx * dx + dy * dy;
   }
}
