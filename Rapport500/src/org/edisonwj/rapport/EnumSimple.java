package org.edisonwj.rapport;

/**
* EnumSimple application enumerates and counts all simple polygons
* generated from a set of random points.
* 
* @author William Edison
* @version 5.00 October 2020
*
* Copyright 2003 William J. Edison
* The code may be freely reused.
*/

import java.io.*;
import java.text.*;
import java.util.*;

public class EnumSimple implements RapportDefaults
{
	protected static final double epsilon = DEFAULTEPSILON;
	protected static final boolean exact_comp = EXACT_COMP;
	protected static final int debug = 1;
	protected static final String[] shapes = {"P", "T", "C", "M", "*", "S", "N"};
								/* P = simple polygon, T = triangle, C = convex  */
								/* M = monotone, * = star, S = spiral, N = other */

	private static int t;		/* Total number of trials to run */
	static int tr;		/* Current trial number */
	static int n;		/* Number of random points */
	static int p;		/* Minimum length of combination to return */
	static int k;		/* Combination generation index */

	static int[] x;		/* Combination generation array */
	static Pointd[] randomPoints;	/* Random points */

	// Random number source
	static long seed;
	static Rand mr;

	// Thread control
	static int nthread;	/* Number of threads to run */

	// Metric arrays
	static long[][] totalSuccess;
	static long[][] totalFail;
	static long[][] totalConvex;
	static long[][] totalMono;
	static long[][] totalStar;
	static long[][] totalMonoStar;
	static long[][] totalSpiral;
	static long[][] totalOther;
	static long[][] totalWlCnt;
	static long totalCount;

	// Timing information
	static long[][] totalDf;

	static Metrics metobj;			/* Metrics object */

	static Date now;
	static Date[] startDate;
	static Date[] endDate;

	/*
	 * Main program for enumerating and counting all simple polygons
	 * generated from a set of random points.
	 * Execution parameters (defaults) (Use . to pass over a parameter):
	 *		Number of random points (12)
	 *		Minimum size of n-gon to be generated (3)
	 *		Number of trials (4)
	 *		Number of threads (4)
	 *		Delay execution to specified time (none)
	 *			DateTime format: yyyy/mm/dd/hh:mm
	 *		Random number seed (based on time of day clock)
	 */
	public static void main(String[] args)
	{
		EnumSimple es;


		// Set number of random points per trial, min. n-gon size,
		// number of trials, number of threads, and seed.
		n = 12;
		if (args.length > 0 && !args[0].equals("."))
			n = Integer.parseInt(args[0]);

		p = 3;
		if (args.length > 1 && !args[1].equals("."))
			p = Integer.parseInt(args[1]);

		t = 4;
		if (args.length > 2 && !args[2].equals("."))
			t = Integer.parseInt(args[2]);

		nthread = 4;
		if (args.length > 3 && !args[3].equals("."))
			nthread = Integer.parseInt(args[3]);

		if (args.length > 4 && !args[4].equals("."))
			delayStart(args[4]);

		// A single seed and subsequent pseudo random number sequence is used
		// to drive all of the trials given the relatively small number of
		// random numbers required, generally << 10,000.
		// Knuth, Art of Computer Programming, Vol. 2, p. 184.
		if (args.length > 5 && !args[5].equals("."))
			seed = Long.parseLong(args[5]);
		else
			seed = Math.abs(System.currentTimeMillis());
		System.out.println("Num. points = " + n +
							", Min. n-gon = " + p +
							", Num. trials = " + t +
							", Num. threads = " + nthread +
							", Seed = " + seed + "\n");
		setupTotal();

		metobj = new Metrics();		/* Create metrics object */

		// Run trials.
		for (int i = 0; i < t; i++)
		{
			es = new EnumSimple(i);

			// Print results after each trial
			// Note: results are cumulative

//			printCounts(i+1);
//			stats(i+1);
		}

		printCounts(t);
		stats(t);

		metobj.printStats();		/* Print final metrics statistics */

//		printCounts();
//		stats();
	}

	/*
	 * Delays start of a run to the specified time.
	 */
	private static void delayStart(String startTime)
	{
		int yr = 0;
		int mo = 0;
		int dy = 1;
		int hr = 0;
   		int mn = 0;
   		long curr_millis, exec_millis, wait_millis;

		StringTokenizer st = new StringTokenizer(startTime, "/:");
		yr = Integer.parseInt(st.nextToken())-1900;
		mo = Integer.parseInt(st.nextToken())-1;
        dy = Integer.parseInt(st.nextToken());
		hr = Integer.parseInt(st.nextToken());
        mn = Integer.parseInt(st.nextToken());

        curr_millis = System.currentTimeMillis();
        Date exec_date = new Date(yr, mo, dy, hr, mn);
        exec_millis = exec_date.getTime();
        if (exec_millis > curr_millis + 60000)
        {
        	wait_millis = exec_millis - curr_millis;
        	System.out.println("Waiting: " + wait_millis/1000 + " sec, Until: " + exec_date);
        	try
        	{
        		Thread.sleep(wait_millis);
        	}
        	catch (InterruptedException e) {};
        }
	}

	/*
	 * Initializes total counts arrays.
	 */

	private static void setupTotal()
	{
		mr = new Rand(seed);
		totalSuccess	= new long[t][];
		totalFail		= new long[t][];
		totalConvex		= new long[t][];
		totalMono		= new long[t][];
		totalStar		= new long[t][];
		totalMonoStar	= new long[t][];
		totalSpiral		= new long[t][];
		totalOther		= new long[t][];
		totalWlCnt		= new long[t][];

		totalDf			= new long[t][];

		startDate		= new Date[t];
		endDate			= new Date[t];

	}

	/*
	 * Enumerates all combinations and unique ring permutations of a set of points.
	 */

	public EnumSimple(int trial)
	{
		tr = trial;
        setupTrial();

//		Generate a random set of points
		randomPoints = new Pointd[n];
		for ( int i=0; i < n; i++ )
		{
			randomPoints[i] = new Pointd(mr.uniform(), mr.uniform());
//			System.out.println("rp[" + i + "]= " + randomPoints[i]);
		}
//		System.out.println();


//		Start threads to loop thru combinations and permutations
//		checking for polygons.

		EnumPermProc[] th = new EnumPermProc[nthread];
		for (int i = 0; i < nthread; i++)
		{
			th[i] = new EnumPermProc(i, randomPoints);
			th[i].start();
		}
		try
		{
			for (int i = 0; i <= nthread; i++)
				th[i].join();
		} catch (Exception e) {}
		endTrial();

	}

	/*
	 * Sets up a trial.
	 */

	private static void setupTrial()
	{
		now = new Date();
	    startDate[tr] = new Date(now.getYear(),
	    	now.getMonth(),
			now.getDate(),
			now.getHours(),
			now.getMinutes(),
			now.getSeconds());

		System.out.println("Start trial: " + (tr+1) + " of " + t
				+ " on " + n + " items at "
				+ startDate[tr].toLocaleString());

		totalSuccess[tr]	= new long[n+1];
		totalFail[tr]		= new long[n+1];
		totalConvex[tr]		= new long[n+1];
		totalMono[tr]		= new long[n+1];
		totalStar[tr]		= new long[n+1];
		totalMonoStar[tr]	= new long[n+1];
		totalSpiral[tr]		= new long[n+1];
		totalOther[tr]		= new long[n+1];
		totalWlCnt[tr]		= new long[n+1];
		totalCount = 0;

		totalDf[tr]			= new long[6];

	//	Setup combination generation array
		x = new int[n+1];
		k = 1;
		x[k] = 1;
	}

	/*
	 * Computes end of trial timing information
	 */
	private static void endTrial()
	{
		now = new Date();
		endDate[tr] = new Date(
			now.getYear(),
			now.getMonth(),
			now.getDate(),
			now.getHours(),
			now.getMinutes(),
			now.getSeconds() );
		long duration = (endDate[tr].getTime() - startDate[tr].getTime())/1000/60;
		System.out.println("End trial: " + (tr+1) + " of " + t
			+ " on " + n + " items at "
			+ 		endDate[tr].toLocaleString()
			+ " duration (min.) " + duration);
		System.out.println("Total items checked: " + totalCount + "\n");
	}

	/*
	 * Computes and prints statistics for the run.
	 */
	private static void stats(int nt)
	{
		NumberFormat nf1 = NumberFormat.getNumberInstance();
		nf1.setMaximumFractionDigits(6);
		nf1.setMinimumFractionDigits(6);
		nf1.setMinimumIntegerDigits(1);

		NumberFormat nf2 = NumberFormat.getNumberInstance();
		nf2.setMaximumFractionDigits(0);
//		nf2.setMinimumFractionDigits(0);
//		nf2.setMinimumIntegerDigits(4);

		NumberFormat nf3 = NumberFormat.getNumberInstance();
		nf1.setMaximumFractionDigits(8);
		nf1.setMinimumFractionDigits(8);
		nf1.setMinimumIntegerDigits(1);

		double prob;

		double[] avgtot = new double[n+1];
		double[] avgsuc = new double[n+1];
		double[] minsuc = new double[n+1];
		double[] maxsuc = new double[n+1];
		double[] varsuc = new double[n+1];

		double[] avgcon = new double[n+1];
		double[] mincon = new double[n+1];
		double[] maxcon = new double[n+1];
		double[] varcon = new double[n+1];

		double[] avgmon = new double[n+1];
		double[] minmon = new double[n+1];
		double[] maxmon = new double[n+1];
		double[] varmon = new double[n+1];

		double[] avgstr = new double[n+1];
		double[] minstr = new double[n+1];
		double[] maxstr = new double[n+1];
		double[] varstr = new double[n+1];

		double[] avgmst = new double[n+1];
		double[] minmst = new double[n+1];
		double[] maxmst = new double[n+1];
		double[] varmst = new double[n+1];

		double[] avgspr = new double[n+1];
		double[] minspr = new double[n+1];
		double[] maxspr = new double[n+1];
		double[] varspr = new double[n+1];

		double[] avgoth = new double[n+1];
		double[] minoth = new double[n+1];
		double[] maxoth = new double[n+1];
		double[] varoth = new double[n+1];

		double[] avgwct = new double[n+1];
		double[] minwct = new double[n+1];
		double[] maxwct = new double[n+1];
		double[] varwct = new double[n+1];

		double[] avgtdf = new double[6];
		double[] mintdf = new double[6];
		double[] maxtdf = new double[6];
		double[] vartdf = new double[6];
		double[] sumtdf = new double[6];

		double sumall = 0.0;
		double sumsim = 0.0;
		double sumtri = 0.0;
		double sumcon = 0.0;
		double summon = 0.0;
		double sumstr = 0.0;
		double summst = 0.0;
		double sumspr = 0.0;
		double sumoth = 0.0;

		for (int i = 0; i <= n; i++)
		{
			avgtot[i] = 0.0;
			avgsuc[i] = 0.0;
			minsuc[i] = Double.MAX_VALUE;
			maxsuc[i] = 0.0;
			varsuc[i] = 0.0;

			avgcon[i] = 0.0;
			mincon[i] = Double.MAX_VALUE;
			maxcon[i] = 0.0;
			varcon[i] = 0.0;

			avgmon[i] = 0.0;
			minmon[i] = Double.MAX_VALUE;
			maxmon[i] = 0.0;
			varmon[i] = 0.0;

			avgstr[i] = 0.0;
			minstr[i] = Double.MAX_VALUE;
			maxstr[i] = 0.0;
			varstr[i] = 0.0;

			avgmst[i] = 0.0;
			minmst[i] = Double.MAX_VALUE;
			maxmst[i] = 0.0;
			varmst[i] = 0.0;

			avgspr[i] = 0.0;
			minspr[i] = Double.MAX_VALUE;
			maxspr[i] = 0.0;
			varspr[i] = 0.0;

			avgoth[i] = 0.0;
			minoth[i] = Double.MAX_VALUE;
			maxoth[i] = 0.0;
			varoth[i] = 0.0;
		}

		for (int i = 0; i < avgwct.length; i++)
		{
			avgwct[i] = 0.0;
			minwct[i] = Double.MAX_VALUE;
			maxwct[i] = 0.0;
			varwct[i] = 0.0;
		}

		for (int i = 0; i <= n; i++)
		{
			for (int j = 0; j < nt; j++)
			{
				avgtot[i] += (totalSuccess[j][i]+totalFail[j][i]);
				avgsuc[i] += totalSuccess[j][i];
				varsuc[i] += totalSuccess[j][i]*totalSuccess[j][i];
				if (totalSuccess[j][i] < minsuc[i])
					minsuc[i] = totalSuccess[j][i];
				if (totalSuccess[j][i] > maxsuc[i])
					maxsuc[i] = totalSuccess[j][i];

				avgcon[i] += totalConvex[j][i];
				varcon[i] += totalConvex[j][i]*totalConvex[j][i];
				if (totalConvex[j][i] < mincon[i])
					mincon[i] = totalConvex[j][i];
				if (totalConvex[j][i] > maxcon[i])
					maxcon[i] = totalConvex[j][i];

				avgmon[i] += totalMono[j][i];
				varmon[i] += totalMono[j][i]*totalMono[j][i];
				if (totalMono[j][i] < minmon[i])
					minmon[i] = totalMono[j][i];
				if (totalMono[j][i] > maxmon[i])
					maxmon[i] = totalMono[j][i];

				avgstr[i] += totalStar[j][i];
				varstr[i] += totalStar[j][i]*totalStar[j][i];
				if (totalStar[j][i] < minstr[i])
					minstr[i] = totalStar[j][i];
				if (totalStar[j][i] > maxstr[i])
					maxstr[i] = totalStar[j][i];

				avgmst[i] += totalMonoStar[j][i];
				varmst[i] += totalMonoStar[j][i]*totalMonoStar[j][i];
				if (totalMonoStar[j][i] < minmst[i])
					minmst[i] = totalMonoStar[j][i];
				if (totalMonoStar[j][i] > maxmst[i])
					maxmst[i] = totalMonoStar[j][i];

				avgspr[i] += totalSpiral[j][i];
				varspr[i] += totalSpiral[j][i]*totalSpiral[j][i];
				if (totalSpiral[j][i] < minspr[i])
					minspr[i] = totalSpiral[j][i];
				if (totalSpiral[j][i] > maxspr[i])
					maxspr[i] = totalSpiral[j][i];

				avgoth[i] += totalOther[j][i];
				varoth[i] += totalOther[j][i]*totalOther[j][i];
				if (totalOther[j][i] < minoth[i])
					minoth[i] = totalOther[j][i];
				if (totalOther[j][i] > maxoth[i])
					maxoth[i] = totalOther[j][i];
			}

			if (i == 0)
			{
				sumall += avgtot[i];
				sumsim += avgsuc[i];
				sumcon += avgcon[i];
				summon += avgmon[i];
				sumstr += avgstr[i];
				summst += avgmst[i];
				sumspr += avgspr[i];
				sumoth += avgoth[i];
			}

			if (i == 3)
				sumtri += avgcon[i];

			avgtot[i] = avgtot[i]/nt;
			varsuc[i] = (nt*varsuc[i] - avgsuc[i]*avgsuc[i])/(nt*(nt-1));
			avgsuc[i] = avgsuc[i]/nt;

			varcon[i] = (nt*varcon[i] - avgcon[i]*avgcon[i])/(nt*(nt-1));
			avgcon[i] = avgcon[i]/nt;

			varmon[i] = (nt*varmon[i] - avgmon[i]*avgmon[i])/(nt*(nt-1));
			avgmon[i] = avgmon[i]/nt;

			varstr[i] = (nt*varstr[i] - avgstr[i]*avgstr[i])/(nt*(nt-1));
			avgstr[i] = avgstr[i]/nt;

			varmst[i] = (nt*varmst[i] - avgmst[i]*avgmst[i])/(nt*(nt-1));
			avgmst[i] = avgmst[i]/nt;

			varspr[i] = (nt*varspr[i] - avgspr[i]*avgspr[i])/(nt*(nt-1));
			avgspr[i] = avgspr[i]/nt;

			varoth[i] = (nt*varoth[i] - avgoth[i]*avgoth[i])/(nt*(nt-1));
			avgoth[i] = avgoth[i]/nt;
		}

		for (int i = 0; i < avgwct.length; i++)
		{
			for (int j = 0; j < nt; j++)
			{
				avgwct[i] += totalWlCnt[j][i];
				varwct[i] += totalWlCnt[j][i]*totalWlCnt[j][i];
				if (totalWlCnt[j][i] < minwct[i])
					minwct[i] = totalWlCnt[j][i];
				if (totalWlCnt[j][i] > maxwct[i])
					maxwct[i] = totalWlCnt[j][i];
			}
			varwct[i] = (nt*varwct[i] - avgwct[i]*avgwct[i])/(nt*(nt-1));
			avgwct[i] = avgwct[i]/nt;
		}

		System.out.println("Simple");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgsuc[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println( "Avg[" + i + "]=" + nf2.format(avgsuc[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(minsuc[i]) +
								"  Max= " + nf2.format(maxsuc[i]) +
								"  Std= " + nf1.format(Math.sqrt(varsuc[i])) +
								"  of " + avgtot[i]);
		}
		System.out.println();

		System.out.println("Convex");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgcon[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println("Avg[" + i + "]=" + nf2.format(avgcon[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(mincon[i]) +
								"  Max= " + nf2.format(maxcon[i]) +
								"  Std= " + nf1.format(Math.sqrt(varcon[i])) +
								"  tof " + comb(n,i));
		}
		System.out.println();

		System.out.println("Monotone");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgmon[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println("Avg[" + i + "]=" + nf2.format(avgmon[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(minmon[i]) +
								"  Max= " + nf2.format(maxmon[i]) +
								"  Std= " + nf1.format(Math.sqrt(varmon[i])) );
		}
		System.out.println();

		System.out.println("Star");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgstr[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println("Avg[" + i + "]=" + nf2.format(avgstr[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(minstr[i]) +
								"  Max= " + nf2.format(maxstr[i]) +
								"  Std= " + nf1.format(Math.sqrt(varstr[i])) );
		}
		System.out.println();

		System.out.println("Monotone and Star");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgmst[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println("Avg[" + i + "]=" + nf2.format(avgmst[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(minmst[i]) +
								"  Max= " + nf2.format(maxmst[i]) +
								"  Std= " + nf1.format(Math.sqrt(varmst[i])) );
		}
		System.out.println();

		System.out.println("Spiral");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgspr[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println("Avg[" + i + "]=" + nf2.format(avgspr[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(minspr[i]) +
								"  Max= " + nf2.format(maxspr[i]) +
								"  Std= " + nf1.format(Math.sqrt(varspr[i])) );
		}
		System.out.println();

		System.out.println("Other");
		for (int i = 0; i <= n; i++)
		{
			if (avgtot[i] > 0.0)
				prob = avgoth[i]/avgtot[i];
			else
				prob = 0.0;
			System.out.println("Avg[" + i + "]=" + nf2.format(avgoth[i]) +
								"\tPrb= " + nf1.format(prob) +
								"  Min= " + nf2.format(minoth[i]) +
								"  Max= " + nf2.format(maxoth[i]) +
								"  Std= " + nf1.format(Math.sqrt(varoth[i])) );
		}
		System.out.println();

		System.out.println("WedgeList Size");
		for (int i = 0; i < avgwct.length; i++)
			System.out.println("Avg[" + i + "]=" + avgwct[i] +
								"  Min= " + minwct[i] +
								"  Max= " + maxwct[i] +
								"  Std= " + nf1.format(Math.sqrt(varwct[i])) );
		System.out.println();

		double avgtm = 0.0;
		double mintm = Double.MAX_VALUE;
		double maxtm = 0.0;
		long tm;

		for (int j = 0; j < nt; j++)
		{
			tm = (endDate[j].getTime()-startDate[j].getTime())/1000;
			avgtm += tm;
			if (tm < mintm)
				mintm = tm;
			if (tm > maxtm)
				maxtm = tm;
		}

		long duration = (endDate[nt-1].getTime() - startDate[0].getTime())/1000/60;
		avgtm = avgtm/nt;
		System.out.println( "Start: " + startDate[0].toLocaleString() +
							" End: "  + endDate[nt-1].toLocaleString() +
							" Duration: " + duration + " min.");
		System.out.println("Time Avg: " + avgtm + " sec., Min. " + mintm + " sec., Max. "+ maxtm + "sec.\n");

		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < nt; j++)
			{
				avgtdf[i] += totalDf[j][i];
				sumtdf[i] += totalDf[j][i];
			}
		}

		for (int i = 0; i < 6; i++)
			avgtdf[i] = avgtdf[i]/nt;

		System.out.println("\nPolygon totals");
		System.out.println("Total tested=\t\t" + sumall);
		System.out.println("Total simple=\t\t" + sumsim);
		System.out.println("Total triangle=\t\t"+ sumtri);
		System.out.println("Total convex=\t\t"  + sumcon);
		System.out.println("Total monotone=\t\t" + summon);
		System.out.println("Total star=\t\t"   + sumstr);
		System.out.println("Total monostar=\t\t"+ summst);
		System.out.println("Total spiral=\t\t"  + sumspr);
		System.out.println("Total other=\t\t"    + sumoth);
		System.out.println("Total simple non-triangle= \t" + (sumsim-sumtri));
		System.out.println("Total simple non-convex= \t" + (sumsim-sumcon));
		System.out.println("Total simple non-convex non-sprial= \t " + (sumsim-sumcon-sumspr));

		System.out.println("\nTiming Information");
		System.out.println("isSimple= \t"  + nf2.format(sumtdf[0]) +
							" \tpertest= " + nf1.format(sumtdf[0]/sumall));
		System.out.println("isConvex= \t"  + nf2.format(sumtdf[2]) +
							" \tpertest= " + nf1.format(sumtdf[2]/(sumsim-sumtri)));
		System.out.println("isSpiral= \t"  + nf2.format(sumtdf[3]) +
							" \tpertest= " + nf1.format(sumtdf[3]/(sumsim-sumcon)));
		System.out.println("isMonotone= \t"+ nf2.format(sumtdf[4]) +
							" \tpertest= " + nf1.format(sumtdf[4]/(sumsim-sumcon-sumspr)));
		System.out.println("isStar=     \t"+ nf2.format(sumtdf[5]) +
							" \tpertest= " + nf1.format(sumtdf[5]/(sumsim-sumcon-sumspr)));
		System.out.println();
	}

	/*
	 * Prints final counts.
	 */

	private static void printCounts(int nt)
	{
		// Print results
		for (int i = 0; i <= n; i++)
		{
			System.out.print("total[" + i + "]= " );
			for (int j = 0; j < nt; j++)
				System.out.print((totalSuccess[j][i]+totalFail[j][i])+" ");
			System.out.println();
		}
		System.out.println();

//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("simple[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalSuccess[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("fail[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalFail[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("convex[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalConvex[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("monotone[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalMono[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("star[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalStar[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("monostar[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalMonoStar[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("spiral[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalSpiral[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i <= n; i++)
//		{
//			System.out.print("other[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalOther[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
//
//		for (int i = 0; i < totalWlCnt[0].length; i++)
//		{
//			System.out.print("wlsize[" + i + "]= " );
//			for (int j = 0; j < nt; j++)
//				System.out.print(totalWlCnt[j][i]+" ");
//			System.out.println();
//		}
//		System.out.println();
	}

	/*
	 *	Based on:
	 *	Semba, Ichiro. Journal of Algorithms 5, 281-283 (1984).
	 *
	 *	Create all combinations with 3 or more items from n items.
	 *
 	 *	Returns the next combination of length >= p
	 */
	public static synchronized int[] nextComb()
	{
		int [] c = null;
		int nc = 0;
		while (nc == 0 && k > 0)
		{
			if (k >= p)
			{
				nc = k;
				c = new int[nc+1];
				for (int j = 1; j <= k; j++)
					c[j] = x[j];
			}

			if (x[k] == n)
			{
				k--;
				x[k]++;
			}

			else
			{
				k++;
				x[k] = x[k-1] + 1;
			}
		}
		return c;
	}

	/*
	 * Computes total counts from an experiment.
	 */

	public static synchronized void totalCounts(long[] success,
											long[] fail,
											long[] convex,
											long[] mono,
											long[] star,
											long[] monostar,
											long[] spiral,
											long[] other,
											long[] wlsize,
											long pcnt,
											long[] df)
	{
		for (int i = 0; i <= n; i++)
		{
			totalSuccess[tr][i]	+= success[i];
			totalFail[tr][i]	+= fail[i];
			totalConvex[tr][i]	+= convex[i];
			totalMono[tr][i]	+= mono[i];
			totalStar[tr][i]	+= star[i];
			totalMonoStar[tr][i]+= monostar[i];
			totalSpiral[tr][i]	+= spiral[i];
			totalOther[tr][i]	+= other[i];
		}

		for (int i = 0; i < totalWlCnt[0].length; i++)
			totalWlCnt[tr][i]	+= wlsize[i];

		totalCount				+= pcnt;

		for (int i = 0; i < df.length; i++)
			totalDf[tr][i] 		+= df[i];
	}

	/*
	 * Computes n factorial.
	 */
	public static long factorial(int n)
	{
		long fact = 1;

		if (n > 0 && n < 21)
			for (int i = 1; i <= n; i++)
				fact *= i;
		else
			fact = 0;
		return fact;
	}

	/*
	 * Computes number of combinations of n items taken m at a time.
	 */
	public static long comb(int n, int m)
	{
		long c = 1;
		long d = n;

		for (int i = 1; i <= m; i++)
		{
			c = (c*d)/i;
			d--;
		}
		return c;
	}
}

	/*
	 * Class to obtain a combination and process all requisite permuations.
	 */
	class EnumPermProc extends Thread implements RapportDefaults
	{
		private int n;					/* Number of random points */
		private int threadId;			/* Number to easily identify thread */
		private int nv;					/* Number of values */
		private int[] a;				/* Permutation values */
		private int[] c;				/* Combination values */
		private int[] b;				/* Permutation generation */
		private boolean[] d;			/* Permutation generation */
		private Pointd[] randomPoints;	/* Base array of random points */
		private Pointd[] v;				/* Array for polygon vertices */
		private Pointd[] u;				/* Utility polygon copy array */
		private double[] pang;			/* Polar angles of polygon */
		private double[] rang;			/* Relativized polar angles of polygon */
		private double[] xang;			/* External angles of polygon */

		private boolean first;			/* Control value for perm generation */
		private long tp;				/* Total number of perms for a comb */
		private long count;				/* Count of permutations checked */
		private long newcount;
		private long oldcount;

		// Metric arrays
		private long[] success;
		private long[] fail;
		private long[] convex;
		private long[] mono;
		private long[] star;
		private long[] monostar;
		private long[] spiral;
		private long[] other;
		private long[] wlszcnt;

		// Timing information
		private long[] df;

		// Wedgelist variables
		private WedgeList wl;			/* Wedgelist for polygon type checking */
		private int wlsize;				/* Number of entries in the wedgelist */
		private int wlmult;				/* Multiplicity of first entry in the wedgelist */

		public int polyCount;
		
		// Number print formating
		NumberFormat nf = NumberFormat.getNumberInstance();

		public EnumPermProc(int ti, Pointd[] rp)
		{
			threadId = ti;
			randomPoints = rp;
			n = randomPoints.length;

			success	= new long[n+1];
			fail	= new long[n+1];
			convex	= new long[n+1];
			mono	= new long[n+1];
			star	= new long[n+1];
			monostar= new long[n+1];
			spiral	= new long[n+1];
			other	= new long[n+1];
			wlszcnt	= new long[n+1];
			count = 0;
			newcount = 0;
			oldcount = 0;

			df		= new long[6];

			nf.setMaximumFractionDigits(4);
			nf.setMinimumFractionDigits(4);
			nf.setMinimumIntegerDigits(1);
		}

		/*
		 * Run a permutation generation and polygon checking thread.
		 * Generates all the permutations and possible polygons for
		 * a given combination of vertices.
		 */
		public void run()
		{
			String print_file = "gener.txt";
			try {
				PrintWriter print_out = new PrintWriter(print_file);
				
				
			while (true)			/* Find polygons */
			{
				c = EnumSimple.nextComb();
				if (c == null)
					break;
//				showComb(threadId, c);

				a = new int[c.length];
				nv = c.length-1;
				v = new Pointd[nv];
				u = new Pointd[nv];
				pang = new double[nv];
				rang = new double[nv];
				xang = new double[nv];

//				Create all ring permutations of the combination
				tp = EnumSimple.factorial(nv - 1)/2;
//				System.out.println("nv= " + nv + ", tp= " + tp);

//				String key;
//				HashMap hm = new HashMap();

				first = true;
				for (long i = 1; i <= tp; i++)
				{
					nextPerm();
					count++;

//					Check unique permutation
//					showPerm(threadId, c, a, count);
//					key = permKey(a);
//					System.out.println(i + " a: " + key);
//					if (hm.containsKey(key))
//						System.out.println("Key already present 1: " + key);
//					else
//						hm.put(key, null);

//					Check all other key cycles
//					for (int k = 1; k < a.length; k++)
//					{
//						key = cycleKey(a, k);
//						System.out.println("testckey = " + key);
//						if (hm.containsKey(key))
//							System.out.println("Key already present 2: " + key);
//					}

//					for (int k = 0; k < a.length; k++)
//					{
//						key = rcycleKey(a, k);
//						System.out.println("testrkey = " + key);
//						if (hm.containsKey(key))
//							System.out.println("Key already present 3: " + key);
//					}

					for (int j = 0; j < nv; j++)
						v[j] = randomPoints[a[j]-1];

//					showPoly(threadId, v);

					countPoly(print_out);

					newcount = count/1000000000;
					if (newcount > oldcount)
					{
						oldcount = newcount;
						System.out.println("Thread: " + threadId +
										" Checked: " + count +
										" Found: " + success[0] +
										" at " + (new Date().toLocaleString()));
					}

				} /* End of permutation loop */

			} /* End of loop through all combination */
			print_out.close();
			}
		catch (IOException e) {
			System.out.println("File error: " + e);
		}

			EnumSimple.totalCounts(success,
								fail,
								convex,
								mono,
								star,
								monostar,
								spiral,
								other,
								wlszcnt,
								count,
								df);

	}

		/*
		 * Adapted from Johnson-Trotter algorithm as described in:
		 * Sedgewick. Permutation Generation Methods.
		 * ACM Computing Surveys, Vol. 9, No. 2, June 1977.
		 */
		private void nextPerm()
		{
			if (first)
			{
				// Consider c[1] fixed. Permute remaining values.
				for (int i = 1; i <= nv; i++)
					a[i-1] = c[i];

				b = new int[nv];
				d = new boolean[nv];
				for (int i = 2; i < nv; i++)
				{
					b[i] = 1;
					d[i] = true;
				}
				b[1] = 0;
				first = false;
				return;
			}

			else
			{
				int k = 0;
				int i = nv-1;
				int x = 0;
				while(b[i] == i)
				{
					if (!d[i])
						x++;
					d[i] = !d[i];
					b[i] = 1;
					i--;
				}

				while (i > 1)
				{
					if (d[i])
						k = b[i]+x;
					else
						k=i-b[i]+x;
					swap(k, k+1);
					b[i] = b[i]+1;
					return;
				}
			}
		}

		/*
		 * Perm swap for permutation generation.
		 */
		private void swap(int i, int j)
		{
			int temp;
			temp = a[i];
			a[i] = a[j];
			a[j] = temp;
		}

		private String permKey(int[] ia)
		{
			String s = "";
			for ( int i = 0; i < ia.length; i++)
				s += ia[i];
			return s;
		}

		private String permRevKey(int[] ia)
		{
			String s = "";
			s += ia[0];
			for ( int i = ia.length-1; i > 0; i--)
				s += ia[i];
			return s;
		}

		private String cycleKey(int[] ia, int k)
		{
			String s = "";
			for (int i = 0; i < ia.length; i++)
				s += ia[(k + i)%ia.length];
			return s;
		}

		private String rcycleKey(int[] ia, int k)
		{
			String s = "";
			for (int i = a.length; i > 0; i--)
				s += ia[(k + i)%ia.length];
			return s;
		}

		/*
		 * Determine shapes and count polygons.
		 */
		private void countPoly(PrintWriter print_out)
		{
			// Mark shape with array shapeb as follows:
			//		0 = simple polygon, 1 = triangle, 2 = convex
			//		3 = monotone, 4 = star, 5 = spiral, 6 = other
			boolean[] shapeb = new boolean[(EnumSimple.shapes).length];
			boolean shapefound = false;
			Date dst;
			long df0, df1, df2, df3;

			dst = new Date();
			shapeb[0] = isSimple();
			df[0] += (new Date().getTime())-dst.getTime();
			if (shapeb[0])
			{
				success[0]++;				/* Simple polygon */
				success[nv]++;
				diag(2,"isSimple");

				// Put vertices in counter clockwise order
				if (!ccw())
					reverse();						

				if (EnumSimple.debug > 2)
				{
					System.out.println("\n#TYPE=1 (Polygon)");
					System.out.println("#NV=" + nv);
					for (int i = 0; i < nv; i++)
						System.out.println(v[i].getx()+"|"+v[i].gety());
					System.out.println("#END");
				}

				if (nv == 3)
				{
					convex[0]++;				/* Triangle polygon */
					convex[nv]++;
					shapeb[1] = true;
					shapeb[2] = true;
					shapeb[3] = true;
					shapeb[4] = true;
					shapefound = true;
					diag(2,"isTriangle");
				}

				else /* Not triangle */
				{
					dst = new Date();
					shapeb[2] = isConvex();
					df[2] += (new Date().getTime())-dst.getTime();
					if (shapeb[2])
					{
						convex[0]++;				/* Convex polygon */
						convex[nv]++;
						shapeb[3] = true;
						shapeb[4] = true;
						shapefound = true;
						diag(2,"isConvex");
					}

					else /* Not convex */
					{
						computePolar();
						computeExternal();

						dst = new Date();
						shapeb[5] = isSpiral();
						df[3] += (new Date().getTime())-dst.getTime();
						if (shapeb[5])
						{
							spiral[0]++;			/* Spiral polygon */
							spiral[nv]++;
							shapefound = true;
							diag(2,"isSpiral");
						}

						else /* Not spiral */
						{
							// Check monotone - compute polar angles, external angles, and wedgelist
							dst = new Date();
							wl = new WedgeList(rang);
							shapeb[3] = isMonotone();
							df[4] += (new Date().getTime())-dst.getTime();
							wlsize = wl.size();
							wlmult = wl.getFirstMult();
							if ( wlsize == 1 && wlmult == 1 )
								System.out.println("*****Non-convex polygon with wlsize = 1 and wlmult = 1");

							// Check star
							dst = new Date();
							shapeb[4] = isStar();
							df[5] += (new Date().getTime())-dst.getTime();

							if (shapeb[3] && !shapeb[4])
							{
								mono[0]++;				/* Monotone polygon */
								mono[nv]++;
								shapefound = true;
								diag(2,"isMonotone");
							}

							if (!shapeb[3] && shapeb[4])
							{
								star[0]++;				/* Star polygon */
								star[nv]++;
								shapefound = true;
								diag(2,"isStar");
							}

							if (shapeb[3] && shapeb[4])	/* Both monotone and star */
							{
								monostar[0]++;
								monostar[nv]++;
								shapefound = true;
								diag(2,"isMonoStar");
							}

//							if (shapeb[5] && (shapeb[3] || shapeb[4]))
//								System.out.println("Shape exception: spiral= " + shapeb[5] +
//																" monotone= " + shapeb[3] +
//																" star= " + shapeb[4]);

						} /* End else not spiral */
					} /* End else not convex */
				} /* End else not triangle */

				if (!shapefound)
				{
					other[0]++;			/* Other polygon */
					other[nv]++;
					shapeb[6] = true;
					diag(2,"isOther");
				}
				
				/* Write data for Draw3D */
				
				int filter = 0;
				if (shapeb[filter] == true) { 
					String testLine = "DataGroup: " + polyCount++ + "\nPolygon:";
					for (int i = 0; i < nv; i++)
						testLine = testLine + " ( " + 
									String.format("%.6f",v[i].getx()) + " , " +
									String.format("%.6f",v[i].gety()) + " , " +
									String.format("%.6f",0.0) + " ) ,";
					testLine = testLine + " # ,  true ,  true ,  false ,  LINE ,  NONE ,  0xfaebd7ff";
					print_out.println(testLine);
				}

				// Compute metrics
				EnumSimple.metobj.count(v);

			} /* End if simple */

			else /* Not simple */
			{
				fail[0]++;						/* Not a simple polygon */
				fail[nv]++;
				diag(2,"notSimple");
			}
		}

	/*
	 * Check for simple polygon.
	 */
	private boolean isSimple()
	{
		/* First let's check that vertices are distinct */

  		for(int i=0; i<nv-1; i++)
    		for(int j=i+1; j<nv; j++)
				if ( v[i].equals(v[j]) )
					return false;

  		/* Now we check simplicity: no two edges should intersect */
		/* improperly. Run through the edges 1,...,n-1 and check  */
		/* them against 2,...,n                                   */

		/* Check the edge from v[i] to v[next(i,n)] */
		/* against the edge starting from v[j]      */

  		for(int i = 0; i < nv-1; i++)
    		for(int j = i+1; j < nv; j++)
				if ( ! valid_edges(v[i],v[next(i,nv)], v[j],v[next(j,nv)]) )
					return false;

		return true;
	}

	/*
	 * Check for convex polygon.
	 */
	private boolean isConvex()
	{
		int i2, i3;
		for (int i1 = 0; i1 < nv; i1++)
		{
			i2 = next(i1, v.length);
			i3 = next(i2, v.length);
			if ( !lefton(	v[i1], v[i2], v[i3]) )
				return false;
		}
		return true;

//		if (wl.size() == 1 && wl.getFirstMult() == 1)
//			return true;
//		else
//			return false;
	}

	/*
	 * Check for monotone polygon.
	 * Preparata and Supowit, Info. Proc. Lett. 12(4) (1981) 161-164.
	 */
	private boolean isMonotone()
	{
		/* Find polar diagram */
//		if (pang == null)
//			computePolar();

		/* Find antipodal wedges with multiplicity 1 */
//		if (wl == null)
//			wl = new WedgeList(rang);

		return wl.findMonotone();
	}

	/*
	 * Check for star polygon
	 */
	private boolean isStar()
	{
		KernelList kl = new KernelList(v);
		return kl.isStar();
	}

	/*
	 * Check for spiral polygon
	 * R. Cole and M. Goodrich. "Optimal Parallel Algorithms for Polygon and Point-Set Problems."
	 * ACM Proceedings of the 4th Annual Symposium on Computational Geometry, 1988, p. 202.
	 */
	private boolean isSpiral()
	{
		int i, j;

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
		nf.setMinimumIntegerDigits(1);

//		/* Compute polar angles */
//		if (pang == null)
//			computePolar();

//		/* Compute external angles */
//		if (xang == null)
//			computeExternal();

		/* Compute curvature sums */

		double[] ai0 = new double[nv];
		double[] a0i = new double[nv];
		double[] fi = new double[nv];
		double[] bi = new double[nv];
		double low;

//		System.out.println("\nSpiral check");
//		for (i = 0; i < nv; i++)
//			System.out.println("v[" + i + "]= " + v[i]);
//		System.out.println();

		a0i[0] = 0;
		fi[1] = a0i[0];
		low  = a0i[0];
		for (i = 1; i < nv; i++)
		{
			a0i[i] = a0i[i-1] + xang[i];
//			System.out.println( "a0i["  + i     + "]= " + nf.format(a0i[i]*180.0/Math.PI) +
//								" a0i[" + (i-1) + "]= " + nf.format(a0i[i-1]*180.0/Math.PI) +
//								" xang["+ i     + "]= " + nf.format(xang[i]*180.0/Math.PI));

			if (i > 1)
				fi[i] = Math.max(fi[i-1],a0i[i-1]);
//			System.out.println("fi[" + i + "]= " + nf.format(fi[i]*180.0/Math.PI));
			if (a0i[i] < low)
				low = a0i[i];
//			System.out.println("low= " + nf.format(low*180.0/Math.PI));
		}
		fi[0] = Math.max(fi[nv-1],a0i[nv-1]);
//		System.out.println("fi[0]= " + nf.format(fi[0]*180.0/Math.PI));
//		System.out.println();

		ai0[0] = 0;
		bi[nv-1] = xang[0];
		for (i = nv-1; i > 0; i--)
		{
			ai0[i] = ai0[next(i,nv)] + xang[next(i,nv)];
//			System.out.println( "ai0[" + i     + "]= " + nf.format(ai0[i]*180.0/Math.PI) +
//								" ai0[" + next(i,nv) + "]= " + nf.format(ai0[next(i,nv)]*180.0/Math.PI) +
//								" xang["+ next(i,nv) + "]= " + nf.format(xang[next(i,nv)]*180.0/Math.PI));

			if (i < nv-1)
				bi[i] = Math.max(bi[next(i,nv)],ai0[i]);
//			System.out.println("bi[" + i + "]= " + nf.format(bi[i]*180.0/Math.PI));
		}
		bi[0] = ai0[0];
//		System.out.println("bi[0]= " + nf.format(bi[0]*180.0/Math.PI));
//		System.out.println();

		double pi3 = 3.0*Math.PI;
		if (fi[0] - low >= pi3)
		{
//			System.out.println("Spiral true - low check");
			return true;
		}

		for (i = 0; i < nv; i++)
			if (fi[i] + bi[i] >= pi3)
			{
//				System.out.println("Spiral true - sum check - i= " + i);
				return true;
			}

//		System.out.println("Spiral false");
		return false;
	}

	/*
	 * Compute polar and relativized polar angles of polygon.
	 */
	private void computePolar()
	{
		// Sets u (point utility array) and pang (polar angles),
		// and rang (relativized polar angles).

		int i, j;

		// Translate all edges to start at origin
		for (i = 0; i < nv; i++)
		{
			j = next(i, nv);
			u[i] = new Pointd(v[j].getx()-v[i].getx(),
					  		  v[j].gety()-v[i].gety() );
		}

		// Find polar angles
//		rang = new double[v.length];
		for (i = 0; i < pang.length; i++)
		{
			double x = u[i].getx();
			double y = u[i].gety();
			double atan = Math.atan(y/x);
			if ( x >= 0.0 && y >= 0.0 )
				pang[i] = atan;
			else if ( x < 0.0 && y >= 0.0 )
				pang[i] =  Math.PI + atan;
			else if ( x < 0.0 && y < 0.0 )
				pang[i] = Math.PI + atan;
			else
				pang[i] = 2*Math.PI + atan;
		}

		if (EnumSimple.debug > 2)
		{
			for (i = 0; i < pang.length; i++)
				System.out.println("pang[" + i + "]= " + pang[i] + " " + (pang[i]*180.0/Math.PI) );
			System.out.println();
		}

		// Find edge with least polar angle.
		j = findMin(pang);
		double mina = pang[j];

		// Relativize all edge angles to edge with least angle.
		// Effectively rotate all edges clockwise, so least angle is zero.
		// Make the least angle (edge) the first.

//		rang = new double[pang.length];
		for (i = 0; i < pang.length; i++)
		{
			rang[i] = pang[j] - mina;
			j = next(j, pang.length);
		}

		if (EnumSimple.debug > 2)
		{
			for (i = 0; i < rang.length; i++)
				System.out.println("rang[" + i + "]= " + rang[i] + " " + (rang[i]*180.0/Math.PI) );
			System.out.println();
		}
	}

	/*
	 * Find minimum value.
	 */
	private int findMin(double[] a)
	{
		int mini = 0;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < a.length; i++)
		{
			if (a[i] < min)
			{
				min = a[i];
				mini = i;
			}
		}

		return mini;
	}

	/*
	 * Computes external angles from polar angles.
	 */
	private void computeExternal()
	{
		/* Sets xang (external angles array) */

		int i, j;

		/* Find external angles at each vertex */
		double diff;
		for (i = 0; i < nv; i++)
		{
			j = prev(i, nv);
			diff = pang[i] - pang[j];
			if (Math.abs(diff) > Math.PI)
			{
				if ( pang[i] < Math.PI && pang[j] > Math.PI)
					xang[i] =  (Math.PI*2.0 - pang[j] + pang[i]);
				else if ( pang[j] < Math.PI && pang[i] > Math.PI)
					xang[i] = -(Math.PI*2.0 - pang[i] + pang[j]);
			}
			else
				xang[i] = pang[i] - pang[j];
		}

		if (EnumSimple.debug > 2)
		{
			for (i = 0; i < nv; i++)
				System.out.println("xang[" + i + "]= " + xang[i] + " " + (xang[i]*180.0/Math.PI) );
			System.out.println();
		}
	}

	/*
	 * Show combination.
	 */
	private static synchronized void showComb(int ti, int[] c)
	{
		int id = ti;
		System.out.print("Thread: " + id + " - ShowComb: ");
		for (int i = 1; i < c.length; i++)
			System.out.print(c[i] + " " );
		System.out.println();
	}

	/*
	 * Show permutation.
	 */
	private static synchronized void showPerm(int ti, int[] c, int[] a, long ct)
	{
		int id = ti;
		System.out.print("Thread: " + id + ", ct: " + ct + " - Show Comb/Perm: ");
		for (int i = 1; i < c.length; i++)
			System.out.print(c[i] + " " );
		System.out.print(" - ");
		for (int i = 0; i < a.length-1; i++)
			System.out.print(a[i] + " " );
		System.out.println();
	}

	/*
	 * Show polygon.
	 */
	private static synchronized void showPoly(int ti, Pointd[] v)
	{
		int id = ti;
		System.out.print("Thread: " + id + " - ShowPoly: ");
		for (int i = 0; i < v.length; i++)
			System.out.print(v[i] + " " );
		System.out.println();
	}

	/*
	 * Suppose segments ab and cd are to be edges of a simple polygon.
	 * If they are non-consecutive edges, they had better not intersect.
	 * If they are consecutive we want to allow a single point of
	 * intersection that must be either a or b. This functions checks
	 * out whether ab and cd are valid edges.
	 */
	public boolean valid_edges (Pointd a, Pointd b, Pointd c, Pointd d)
	{
		/* if edges(a,b) and (c,d) are successive, i.e. b==c, check a != d */
		/* or edges(c,d) and (a,b) are successive, i.e. d==a, check c != b */
		if ( b.equals(c) )
		{
			if ( a.equals(d) )
				return false;
		else
			return true;
		}
		if ( d.equals(a) )
		{
			if ( c.equals(b) )
				return false;
		  	else
				return true;
	    }

	     /* Not successive edges. Check for intersection */
		if ( segsegInt(a, b, c, d) != '0')
			return false;
		else
			return true;
	}

	/*
	 * Return true if vertices are in counter clockwise order
	 */
	public boolean ccw()
	{
		if (area2() > 0 )
			return true;
		else
			return false;
	}

 	/*
 	 * Reverses vertex order to make it Ccw
 	 */
 	public void reverse()
 	{
 		System.arraycopy( v, 0, u, 0, nv );
 		for (int i = 0; i < nv; i++)
 			v[i] = u[nv - i - 1];
 	}

	/*
	 * Returns twice the area of the polygon
	 */
 	public double area2()
 	{
		double sum = 0;
		for (int i = 1; i <= nv - 2; i++)
			sum += twiceArea(v[0], v[i], v[i+1]);
		return sum;
 	}

	/*
	 * Compute twice the area of the triangle abc .
	 * Note: if abc are rational or integers, the result stays
	 * the same
	 */
	public double twiceArea (Pointd a, Pointd b, Pointd c)
	{
		return (   a.getx() * b.gety() - a.gety() * b.getx()
				+  b.getx() * c.gety() - b.gety() * c.getx()
				+  c.getx() * a.gety() - c.gety() * a.getx());
	}

	/*
	 * This code is described in "Computational Geometry in C" (Second Edition),
	 * Chapter 7.  It is not written to be comprehensible without the
	 * explanation in that book.
	 *
	 * Compile:  gcc -o segseg segseg.c
	 *
	 * Written by Joseph O'Rourke.
 	 * Last modified: November 1997
	 * Questions to orourke@cs.smith.edu.
	 *
	 * This code is Copyright 1998 by Joseph O'Rourke.  It may be freely
	 * redistributed in its entirety provided that this copyright notice is
	 * not removed.
	 *---------------------------------------------------------------------
	 *
	 *---------------------------------------------------------------------
	 * SegSegInt: Finds the point of intersection p between two closed
	 * segments ab and cd.  Returns p and a char with the following meaning:
	 *  'e': The segments collinearly overlap, sharing a point.
	 *  'v': An endpoint (vertex) of one segment is on the other segment,
	 *		but 'e' doesn't hold.
	 *  '1': The segments intersect properly (i.e., they share a point and
	 *		neither 'v' nor 'e' holds).
	 *  '0': The segments do not intersect (i.e., they share no points).
	 * Note that two collinear segments that share just one point, an endpoint
	 * of each, returns 'e' rather than 'v' as one might expect.
	 *---------------------------------------------------------------------
	 */
	private char segsegInt( Pointd a, Pointd b, Pointd c, Pointd d )
	{
		double  s, t;       /* The two parameters of the parametric eqns. */
		double num, denom;  /* Numerator and denoninator of equations. */
		double px, py;	    /* Coordinates of intersection point. */
		char code = '?';    /* Return char characterizing intersection. */

		denom = a.getx() * (double)( d.gety() - c.gety() ) +
				b.getx() * (double)( c.gety() - d.gety() ) +
				d.getx() * (double)( b.gety() - a.gety() ) +
				c.getx() * (double)( a.gety() - b.gety() );

	/* If denom is zero, then segments are parallel: handle separately. */
		if (denom == 0.0)
			return  ParallelInt(a, b, c, d);

		num =   a.getx() * (double)( d.gety() - c.gety() ) +
				c.getx() * (double)( a.gety() - d.gety() ) +
				d.getx() * (double)( c.gety() - a.gety() );
		if ( (num == 0.0) || (num == denom) )
			code = 'v';			/* non-collinear end-point overlap */

		s = num / denom;

		num = -(a.getx() * (double)( c.gety() - b.gety() ) +
				b.getx() * (double)( a.gety() - c.gety() ) +
				c.getx() * (double)( b.gety() - a.gety() ) );
		if ( (num == 0.0) || (num == denom) )
			code = 'v';			/* non-collinear end-point overlap */

		t = num / denom;

		if    ( (0.0 < s) && (s < 1.0) &&
				(0.0 < t) && (t < 1.0) )
			code = '1';				/* proper intersection */

		else if ( (0.0 > s) || (s > 1.0) ||
				  (0.0 > t) || (t > 1.0) )
			code = '0';				/* no intersection */

//		px = a.getx() + s * ( b.getx() - a.getx() );
//		py = a.gety() + s * ( b.gety() - a.gety() );

		return code;
	}

	/*
	 * Check for parallel intersection
	 */
	private char ParallelInt( Pointd a, Pointd b, Pointd c, Pointd d )
	{
//		Pointd p;
		char code;

	   if ( !collinear( a, b, c) ) {
//		  p = null;
		  code = '0';
	   }
	   else if ( collinearBetween( a, b, c ) ) {
//		  p = c;
		  code = 'e';
	   }
	   else if ( collinearBetween( a, b, d ) ) {
//		  p = d;
		  code = 'e';
	   }
	   else if ( collinearBetween( c, d, a ) ) {
//		  p = a;
		  code = 'e';
	   }
	   else if ( collinearBetween( c, d, b ) ) {
//		  p = b;
		  code = 'e';
	   }
	   else {
//		  p = null;
		  code = '0';
	   }
	  return code;
	}

	/*---------------------------------------------------------------------
	 * Returns TRUE iff point c lies on the closed segement ab.
	 * Assumes it is already known that abc are collinear.
	 *---------------------------------------------------------------------
	 */
	private boolean collinearBetween(Pointd a, Pointd b, Pointd c)
	{
   	/* If ab not vertical, check betweenness on x; else on y. */
   		if ( a.getx() != b.getx() )
   		   return ((a.getx() <= c.getx()) && (c.getx() <= b.getx())) ||
   		          ((a.getx() >= c.getx()) && (c.getx() >= b.getx()));
   		else
   		   return ((a.gety() <= c.gety()) && (c.gety() <= b.gety())) ||
   		          ((a.gety() >= c.gety()) && (c.gety() >= b.gety()));
	}

	/*
	 * Checks if point c is collinear with line ab
	 */
	private boolean collinear (Pointd a, Pointd b, Pointd c)
	{
		double twoArea = twiceArea(a, b, c);

		if (EnumSimple.exact_comp)
		{
			if (twoArea == 0)
				return true;
			else
				return false;
		}
		else
		{
			if (Math.abs(twoArea) <= EnumSimple.epsilon)
				return true;
			else
				return false;
		}
	}

	/*
	 * Checks if point c is left of or on line ab
	 */
	private boolean lefton (Pointd a, Pointd b, Pointd c)
	{
		double twoArea =  twiceArea(a, b, c);

		if (EnumSimple.exact_comp)
		{
			if (twoArea >= 0)
				return true;
			else
				return false;
		}
		else
		{
	    	if ( (twoArea > EnumSimple.epsilon) || (Math.abs(twoArea) <= EnumSimple.epsilon) )
	    		return true;
	    	else
	    		return false;
	  	}
	}

	/*
	 * Return next index modulo n
	 */
	public int next(int i, int n)
	{
	  if (i < n-1) return i+1;
	  else return 0;
	}

	/*
	 * Return previous index modulo n
	 */
	public int prev(int i, int n)
	{
		if (i==0) return n-1;
		else return i-1;
	}

	/*
	 * Print diagnostic messages.
	 */
	private void diag(int i, String s)
	{
		if (EnumSimple.debug >= i)
			System.out.println("\n" + s);
	}
}