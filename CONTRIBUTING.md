# Guide to contributing

Please read this if you intend to contribute to the project.

## Style

If you plan on contributing changes, make sure that your code follows the Ptolemy II style guide, which is available at https://www2.eecs.berkeley.edu/Pubs/TechRpts/2014/EECS-2014-164.html

In particular:
* use camelCase for identifiers.
* document your code.

## Legal

Ptolemy II is made available under a BSD license.  It is best if the the Regents of the University of California hold the copyright.  Please be sure to include the standard Ptolemy II Copyright in any new files.  See [What is the copyright and license?](https://ptolemy.berkeley.edu/ptolemyII/ptIIfaq.htm#ptolemy%20II%20copyright)

## Making your changes

#### Manual setup

Use Eclipse to check out the repo.

See [$PTII/doc/eclipse/index.htm](https://ptolemy.berkeley.edu/ptolemyII/ptIIlatest/doc/eclipse/index.htm)

### Create an Issue
External collaborators who do not have write access to the ptII repo should create a [GitHub Issue](https://github.com/icyphy/ptII/issues)

Internal collaborators could consider creating an issue for every significant piece of work ( > 2 hrs).

### Create a new branch for your changes

1. In the Git Repositories tab, expand the ptII repository.
2. Right click on the "Branches" node and select "Switch To" -> "New Branch".  
3. Enter the new branch name.  
Branch name should be {GitHubUserName}/{summary or issue id} e.g. ``elvis/integrate-display-actor``.  
Alternative idea is a bit more elaborated : {GitHubUserName}/{ChangeType}/{issue id}/{summary} e.g. ``jake/ft/5/integrate-display-actor``. In this approach change type acronyms can be e.g. ft (feature i.e. with functional value) ; eh (enhancement without functional value) ; bg (bug) ; doc ; ...

### Committing
* Make your changes.
* Make sure you include tests.
* Make sure the test suite passes after your changes.
* Commit your changes into that branch. 
* For files that are in Eclipse packages, right click on the file in the Package Explorer and commit it.  
* For files that are not in Eclipse packages, invoke the Git Staging via Window -> Show View -> Other -> Git -> Git
* Use descriptive and meaningful commit messages. See [git commit records in the Eclipse Project Handbook](https://www.eclipse.org/projects/handbook/#resources-source).  Mention issue_id in each commit comment using syntax like "Adapt this interface for #15" to link to issue 15.
* Make sure you use the sign off your commit.
  * If you are commiting using Eclipse, then click on the signature button  
  * If you are invoking git from the command line, then use the `-s` flag.  
  * If you are using some other tool, add ``Signed-off-by: YourFirstName YourLastName <YourEclipseAccountEmailAddress>`` For example: ``Signed-off-by: Christopher Brooks <cxh@eecs.berkeley.edu>``
* Push your changes to your branch in your forked repository.

## Submitting the changes

1. Submit a pull request via the normal [GitHub UI](https://github.com/icyphy/ptII) to trigger to request feedback / code review / ... 
2. Mention issue_id in each comment using syntax like "Adapt this interface for #15" to link to issue 15 in the initial comment for a Pull Request.
3. The pull request will be reviewed by one of the committers, and then merged into master.
 
## After submitting

* Do not use your branch for any other development, otherwise further changes that you make will be visible in the PR.

# Credit

This file is based on a file written by the Vert.x team at https://raw.githubusercontent.com/eclipse/vert.x/master/CONTRIBUTING.md

We have shamelessly copied, modified and co-opted it for our own repo and we graciously acknowledge the work of the original authors.
