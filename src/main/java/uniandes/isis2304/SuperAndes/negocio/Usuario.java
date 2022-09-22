package uniandes.isis2304.SuperAndes.negocio;

public class Usuario implements VOUsuario {

	private long id;
	private long nDocumento;
	private String tipoDocumento;
	private String tipoUsuario;
	private String nombre;
	private String correo;
	private String pais;
	private String ciudad;
	private String direccion;
	private int puntos;
	private long id_Sucursal;
	private long id_Supermercado;
	
	public Usuario() {

		this.id = 0;
		this.nDocumento = 0;
		this.tipoDocumento = "";
		this.tipoUsuario = "";
		this.nombre = "";
		this.correo = "";
		this.pais = "";
		this.ciudad = "";
		this.direccion = "";
		this.puntos = 0;
		this.id_Sucursal = 0;
		this.id_Supermercado = 0;
	}
	
	public Usuario(long id, long nDocumento, String tipoDocumento, String tipoUsuario, String nombre, String correo,
			String pais, String ciudad, String direccion, int puntos, long id_Sucursal, long id_Supermercado) {

		this.id = id;
		this.nDocumento = nDocumento;
		this.tipoDocumento = tipoDocumento;
		this.tipoUsuario = tipoUsuario;
		this.nombre = nombre;
		this.correo = correo;
		this.pais = pais;
		this.ciudad = ciudad;
		this.direccion = direccion;
		this.puntos = puntos;
		this.id_Sucursal = id_Sucursal;
		this.id_Supermercado = id_Supermercado;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getnDocumento() {
		return nDocumento;
	}

	public void setnDocumento(long nDocumento) {
		this.nDocumento = nDocumento;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public int getPuntos() {
		return puntos;
	}

	public void setPuntos(int puntos) {
		this.puntos = puntos;
	}

	public long getId_Sucursal() {
		return id_Sucursal;
	}

	public void setId_Sucursal(long id_Sucursal) {
		this.id_Sucursal = id_Sucursal;
	}

	public long getId_Supermercado() {
		return id_Supermercado;
	}

	public void setId_Supermercado(long id_Supermercado) {
		this.id_Supermercado = id_Supermercado;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Usuario [id=");
		builder.append(id);
		builder.append(", nDocumento=");
		builder.append(nDocumento);
		builder.append(", tipoDocumento=");
		builder.append(tipoDocumento);
		builder.append(", tipoUsuario=");
		builder.append(tipoUsuario);
		builder.append(", nombre=");
		builder.append(nombre);
		builder.append(", correo=");
		builder.append(correo);
		builder.append(", pais=");
		builder.append(pais);
		builder.append(", ciudad=");
		builder.append(ciudad);
		builder.append(", direccion=");
		builder.append(direccion);
		builder.append(", puntos=");
		builder.append(puntos);
		builder.append(", id_Sucursal=");
		builder.append(id_Sucursal);
		builder.append(", id_Supermercado=");
		builder.append(id_Supermercado);
		builder.append("]");
		return builder.toString();
	}
}