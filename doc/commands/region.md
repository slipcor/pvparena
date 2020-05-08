# Region command

## Description

Set up regions for an arena. Oh, and show their borders. ... and set some WIP values

## Usage Examples

Command |  Definition
------------- | -------------
/pa free region         | start setting up a region for the arena "free"
/pa free !r ball        | save the cuboid region "ball" to the arena "free"
/pa ctf !r X spher      | save the spheric region "X" to the arena "ctf"
/pa ctf !r remove X     | remove the region "X" from the arena "ctf"
/pa free !r ball border | shows the border around the region "ball" of the arena "free"

## Details

For a better display of HOW to setup regions, you might want to check out our tutorial section.
Setting up an arena needs 3 things:

`/pa [arenaname] region`
select two points that define the region
`/pa [arenaname] region [regionshape]` 

Valid regionshapes include:
 
- CUBOID (default)
- SPHERIC
- CYLINDRIC