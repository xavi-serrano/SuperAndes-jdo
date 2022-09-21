package uniandes.isis2304.SuperAndes.persistencia;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import uniandes.isis2304.SuperAndes.negocio.PromocionProducto;

class SQLPromocionProducto {
	
	private final static String SQL = PersistenciaSuperAndes.SQL;

	private PersistenciaSuperAndes pp;

	public SQLPromocionProducto (PersistenciaSuperAndes pp)
	{
		this.pp = pp;
	}

	public long adicionarPromocionProducto (PersistenceManager pm, long id_Promocion, long id_Producto) 
	{
        Query q = pm.newQuery(SQL, "INSERT INTO " + pp.darTablaPromocionProducto () + "(id_Promocion, id_Producto) values (?, ?)");
        q.setParameters(id_Promocion, id_Producto);
        return (long) q.executeUnique();
	}

	public long eliminarPromocionProductoPorId (PersistenceManager pm, long id)
	{
        Query q = pm.newQuery(SQL, "DELETE FROM " + pp.darTablaPromocionProducto () + " WHERE id = ?");
        q.setParameters(id);
        return (long) q.executeUnique();
	}

	public PromocionProducto darPromocionProductoPorId (PersistenceManager pm, long id) 
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaPromocionProducto () + " WHERE id = ?");
		q.setResultClass(PromocionProducto.class);
		q.setParameters(id);
		return (PromocionProducto) q.executeUnique();
	}

	public List<PromocionProducto> darPromocionesProductos (PersistenceManager pm)
	{
		Query q = pm.newQuery(SQL, "SELECT * FROM " + pp.darTablaPromocionProducto ());
		q.setResultClass(PromocionProducto.class);
		return (List<PromocionProducto>) q.executeList();
	}
}