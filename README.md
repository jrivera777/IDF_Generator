IDF_Generator
===================

###Program Name: IDFGenerator.jar
####Purpose
~~To generate a set of IDF files based on a given base IDF file 
which should contain parametric objects.~~ Theoretically should generate all
combinations of parametric objects.

**Update:** The program goal is no longer to simply generate IDF files.  That idea created a storage space issue. The large number of possible options would generate gigabytes to terabytes worth of files.  Instead, the application now generates the IDF files a few at a time, runs their EnergyPlus simulations, extracts the required data, and writes them out to one file.


####Need
The issue we face using parametric objects along with
the parametricpreprocessor program is the number of different files that
are generated. Not all combinations are produced.  

An example can illustrate our problem:  

Given an IDF file with parametric objects:  

* A - with options a1, a2, a3
* B - with options b1, b2, b3

The parametricpreprocessor will generate only 3 files. These would have values:  

* file1 -> a1, b1
* file2 -> a2, b2
* file3 -> a3, b3


This is does not describe all possible combinations. If one parametric object
contains fewer options than another, its last option will be reused.

IDF_Generator attempts to generate the following files

* file1 -> a1, b1
* file2 -> a1, b2
* file3 -> a1, b3
* file4 -> a2, b1
* file5 -> a2, b2
* file6 -> a2, b3
* file7 -> a3, b1
* file8 -> a3, b2
* file9 -> a3, b3

If the parametricpreprocessor is given an IDF file with 3 parametric objects,
having ***N***, ***M***, and ***R*** options respectively, where ***N*** is the largest amount, it
will attempt to produce at most ***N*** files.

The IDF_Genator if given the same file will attempt to generate ***N*** x ***M*** x ***R*** files.

These files can then be used to run simulations using [EPLaunch](http://apps1.eere.energy.gov/buildings/energyplus/energyplus_utilities.cfm).


####How To Use: 
**Update:** If you are interested in using the older version of the appliction, see the README [here](https://github.com/jrivera777/IDF_Generator/blob/27c91f08990cd16c647e254875485ff73b7d5cf5/README.md)

0. **BACK UP YOUR BASE IDF FILE BEFORE USE.  THIS IS A WORK IN PROGRESS. NO GAURANTEE IS MADE IN USING THIS SOFTWARE. LOSS OF IDF FILES IS POSSIBLE!!!**

1. Defining Options file:
	* XML file. 
		* Basic file setup is:

			```xml
			<Parametrics>
				<ParametricOption id="OPTION-NAME">
					<Option value="NUMERIC-VALUE-FOR-USE-IN-GA-AND-NAMES">Option Text</Option>
					<Option value="2">Example 2</Option>
					<Option value="3">Example 3</Option>
				</ParametricOption>
			</Parametrics>
			```
		* **OPTION-NAME**: the name of the variable that will be used in the Base IDF file. THEY MUST MATCH!
		* **NUMERIC-VALUE-FOR-USE-IN-GA-AND-NAMES**: the value that will be attached to the end of each file generated.  Should be a number.
	  	This will eventually match up with the options in the genetic algorithm. 
	  
2. Placing Parameters in base IDF file:
	* Place an Option variable where ever you'd like it's counterpart in the Options file to fill it in with values.
	* Parameter variables must have this format: `$VARIABLE-NAME`

3. Running the Application:
	* You can click the IDFGenerator JAR file. It will ask for 
		1. The base IDF file.
		2. The Options XML file.
		3. The weather file for running simulations (.epw)
		4. The directory that contains the Epl-run.bat batch file.
		5. The desired output directory.
	* You can find the Epl-run.bat file in your EnergyPlus install folder, which is usually something like: `"C:\EnergyPlusVX-X-X\"`
	* The resulting output file will be filled with entries that look like `1-1-3-1 : 14567.425`
	  
If you have any questions, concerns, or have found a mistake in this document or the program, please contact me at `jrive034-at-fiu.edu.`
