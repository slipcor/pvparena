# Regions command

## Description

This command displays a list of arena regions / their properties.

## Usage Examples

Command |  Definition
------------- | -------------
/pa free regions  | list all arena regions of "free", and their shape and type
/pa free !rs ball | debug print properties of the region "ball" of the arena "free"

## Details

The list shows: `[regionname]: [regiontype], [regionshape]`

The debug prints:

    [arenaname]:[regionname]
    [region type]
    [region shape]
    [region flags]
    [region protections]
    [first position]
    [second position]