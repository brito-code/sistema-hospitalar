package br.pucminas.hospital.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoProfissional;

class ProfissionalHierarquiaTest {

    private final Paciente paciente = new Paciente("Ana");

    @Test
    void especialistaAtendeApenasSuaEspecialidade() {
        MedicoEspecialista cardiologista = new MedicoEspecialista("Dra. Mariana", "CRM-MG 45231", Especialidade.CARDIOLOGIA);

        assertTrue(cardiologista.atende(Especialidade.CARDIOLOGIA));
        assertTrue(cardiologista.atendeEspecialidade(Especialidade.CARDIOLOGIA));
        assertFalse(cardiologista.atende(Especialidade.ORTOPEDIA));
        assertFalse(cardiologista.atende(null));
        assertEquals(TipoProfissional.ESPECIALISTA, cardiologista.getTipo());
    }

    @Test
    void clinicoECirurgiaoNaoAtendemEspecialidade() {
        ProfissionalSaude clinico = new ClinicoGeral("Dr. Rafael", "CRM-MG 38902");
        ProfissionalSaude cirurgiao = new Cirurgiao("Dr. Lucas", "CRM-MG 51044", "Ortopedica");

        for (Especialidade e : Especialidade.values()) {
            assertFalse(clinico.atendeEspecialidade(e));
            assertFalse(cirurgiao.atendeEspecialidade(e));
        }
        assertNull(clinico.getEspecialidade());
        assertEquals(TipoProfissional.CLINICO_GERAL, clinico.getTipo());
        assertEquals(TipoProfissional.CIRURGIAO, cirurgiao.getTipo());
    }

    @Test
    void todosOsSubtiposSaoMedicos() {
        assertInstanceOf(Medico.class, new ClinicoGeral("A", "CRM-MG 10001"));
        assertInstanceOf(Medico.class, new MedicoEspecialista("B", "CRM-MG 10002", Especialidade.NEUROLOGIA));
        assertInstanceOf(Medico.class, new Cirurgiao("C", "CRM-MG 10003", "Geral"));
    }

    @Test
    void clinicoGeralEncaminhaPacienteParaEspecialidade() {
        ClinicoGeral clinico = new ClinicoGeral("Dr. Rafael", "CRM-MG 38902");

        Encaminhamento encaminhamento = clinico.encaminhar(paciente, Especialidade.NEUROLOGIA);

        assertSame(clinico, encaminhamento.clinicoGeral());
        assertSame(paciente, encaminhamento.paciente());
        assertEquals(Especialidade.NEUROLOGIA, encaminhamento.especialidade());
    }

    @Test
    void encaminhamentoExigePacienteEEspecialidade() {
        ClinicoGeral clinico = new ClinicoGeral("Dr. Rafael", "CRM-MG 38902");

        assertThrows(RegraNegocioException.class, () -> clinico.encaminhar(null, Especialidade.CARDIOLOGIA));
        assertThrows(RegraNegocioException.class, () -> clinico.encaminhar(paciente, null));
    }

    @Test
    void cirurgiaoRegistraProcedimentos() {
        Cirurgiao cirurgiao = new Cirurgiao("Dr. Lucas", "CRM-MG 51044", "Ortopedica");

        assertTrue(cirurgiao.getProcedimentos().isEmpty());
        assertThrows(RegraNegocioException.class, () -> cirurgiao.registrarProcedimento(null));
        assertThrows(UnsupportedOperationException.class, () -> cirurgiao.getProcedimentos().add(null));
    }
}
