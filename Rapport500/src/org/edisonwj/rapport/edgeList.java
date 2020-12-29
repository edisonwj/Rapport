package org.edisonwj.rapport;

/**
 * edgeList implements an edge list structure using the HashMap class.
 * The first vertex for each edge is used as the key for the edge in
 * the hash list.  edgeList also uses an array containing these first
 * vertices in order to generate random selections of edges from the
 * the hash table.                                                  
 */

import java.util.*;

public class edgeList
{
	HashMap edgeHM;
	Pointd [] vertices;
	Rand mr;
	int nv;
	
	edgeList(int n)
	{
		edgeHM = new HashMap();
		vertices = new Pointd[n];
		mr = new Rand();
		nv = 0;
	}
	
	public int length()
	{
		return nv;
	}
	
	public void put(EdgeR e)
	{
		Pointd v1 = e.getv1();
		vertices[nv++] = v1;
		edgeHM.put(v1, e);
	}
	
	public EdgeR get(Pointd v1)
	{
		return (EdgeR)edgeHM.get(v1);
	}
	
	public EdgeR get(int i)
	{
		return (EdgeR)edgeHM.get(vertices[i]);
	}
	
	public EdgeR random()
	{
		Pointd v1 = vertices[mr.uniform(0,nv-1)];
		return (EdgeR)edgeHM.get(v1);
	}
	
	public void replace(EdgeR e, Pointd v3)
	{
		Pointd v1 = e.getv1();
		Pointd v2 = e.getv2();
		EdgeR e1 = new EdgeR(v1, v3);
		EdgeR e2 = new EdgeR(v3, v2);
		vertices[nv++] = v3;
		edgeHM.remove(v1);
		edgeHM.put(v1, e1);
		edgeHM.put(v3, e2);
	}
	
	public PolygonA polygon()
	{
		EdgeR edge;
		edge = (EdgeR)edgeHM.get(vertices[0]);
		for (int i = 0; i < nv; i++)
		{
			vertices[i] = edge.getv1();
			edge = (EdgeR)edgeHM.get(edge.getv2());
		}
		return new PolygonA(vertices);
	}
	
	public Pointd[] getvertices()
		{
			EdgeR edge;
			edge = (EdgeR)edgeHM.get(vertices[0]);
			for (int i = 0; i < nv; i++)
			{
				vertices[i] = edge.getv1();
				edge = (EdgeR)edgeHM.get(edge.getv2());
			}
			return vertices;
	}
	
}

class edgeEnumeration implements Enumeration
{
	private int count;
	private EdgeR edge;
	private Pointd key;
	private edgeList el;
	
	public edgeEnumeration(edgeList el)
	{
		count = 0;
		this.el = el;
		key = el.vertices[0];
		this.el = el;
	}
		
	public boolean hasMoreElements()
	{
		return (count < el.nv);
	}
	
	public Object nextElement()
	{
		edge = (EdgeR)el.get(key);
		key = edge.getv2();
		count++;
		return edge;
	}
}

	