package net.slipcor.pvparena.ncloader;

public class NCLoadable
  implements Cloneable
{
  private final String name;

  public NCLoadable(String name)
  {
    this.name = name;
  }

  public void init()
  {
  }

  public final String getName()
  {
    return this.name;
  }

  public void unload()
  {
  }
}