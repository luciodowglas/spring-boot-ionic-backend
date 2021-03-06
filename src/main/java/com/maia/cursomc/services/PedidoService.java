package com.maia.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maia.cursomc.domain.ItemPedido;
import com.maia.cursomc.domain.Pedido;
import com.maia.cursomc.domain.PgtoBoleto;
import com.maia.cursomc.domain.enums.EstadoPgto;
import com.maia.cursomc.repositores.ItemPedidoRepository;
import com.maia.cursomc.repositores.PagamentoRepository;
import com.maia.cursomc.repositores.PedidoRepository;
import com.maia.cursomc.services.exception.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired // instanciando o Pedido Service
	private PedidoRepository repository;

	@Autowired
	private BoletoService boletoService;

	@Autowired
	private PagamentoRepository pagamentoRepository;

	@Autowired
	private ProdutoService produtoService;

	@Autowired
	private ItemPedidoRepository itemPedidoRepository;

	// metodo para BusarPor ID com SpringDataJPA
	public Pedido find(Integer id) {
		Optional<Pedido> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto Não Encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}

	// inserir Pedido
	public Pedido insert(Pedido obj) {
		obj.setId(null); // garantindo que é um pedido novo
		obj.setDtaPedido(new Date()); // gera a data atual do sistema
		obj.getPagamento().setEstadoPgto(EstadoPgto.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PgtoBoleto) {
			PgtoBoleto pgtBoleto = (PgtoBoleto) obj.getPagamento();
			boletoService.preencherPgtoBoleto(pgtBoleto, obj.getDtaPedido());
		}
		obj = repository.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		
		for (ItemPedido ip : obj.getItens()) {
			ip.setDescont(0.0);
			ip.setPreco(produtoService.find(ip.getProduto().getId()).getPreco());
			ip.setPedido(obj);
		}
		
		itemPedidoRepository.saveAll(obj.getItens());
		
		return obj;
	}

}
